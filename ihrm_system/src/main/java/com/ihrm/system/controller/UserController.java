package com.ihrm.system.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.utils.JwtUtils;
import com.ihrm.domain.system.*;
import com.ihrm.domain.system.response.ProfileResult;
import com.ihrm.domain.system.response.UserResult;
import com.ihrm.domain.system.response.UserSimpleResult;
import com.ihrm.system.client.DepartmentFeignClient;
import com.ihrm.system.service.PermissionService;
import com.ihrm.system.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 观海
 */ //1.解决跨域
@CrossOrigin
//2.声明restController
@RestController
/**
 * 3.设置父路径
 */
@RequestMapping("/sys")
public class UserController extends BaseController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DepartmentFeignClient departmentFeignClient;

    @PostMapping("/user/upload/{id}")
    public Result upload(@PathVariable String id, @RequestParam(name = "file")MultipartFile file) throws IOException {
        //调用service保存图片，获取图片的访问地址
        String imgUrl=userService.uploadImage(id,file);
        return new Result(ResultCode.SUCCESS,imgUrl);
    }

    @RequestMapping(value = "/user/simple", method = RequestMethod.GET)
    public Result simple() throws Exception {
        List<UserSimpleResult> list = new ArrayList<>();
        List<User> users = userService.findAll(companyId);
        for (User user : users) {
            list.add(new UserSimpleResult(user.getId(),user.getUsername()));
        }
        return new Result(ResultCode.SUCCESS,list);
    }

    /**
     * 导入excel添加用户
     */
    @RequiresPermissions(value = "POINT-USER-IMPORT")
    @PostMapping(value = "/user/import",name = "POINT-USER-IMPORT")
    public Result importUser(@RequestParam(name = "file") MultipartFile file){
        //解析excel
        //1.创建工作布
        Workbook wb= null;
        try {
            wb = new XSSFWorkbook(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //2.获取sheet
        Sheet sheet = wb.getSheetAt(0);
        //3.获取sheet中的每一行和每一个单元格
        //获取用户数据列表
        List<User> list=new ArrayList<>();
        for (int rowNum=1;rowNum<=sheet.getLastRowNum();rowNum++){
            Row row = sheet.getRow(rowNum);
            Object [] values=new Object[row.getLastCellNum()];
            for (int callNum=1;callNum<row.getLastCellNum();callNum++){
                Cell cell = row.getCell(callNum);
                //获取每一单元格的内容
                Object value = getCellValue(cell);
                values[callNum]=value;
            }
            User user=new User(values);
            list.add(user);
        }
        //批量保存用户
        userService.saveAll(list,companyId,companyName);
        return new Result(ResultCode.SUCCESS,"导入成功！");
    }

    public static Object getCellValue(Cell cell){
        //获取单元格属性类型
        CellType type = cell.getCellType();
        //根据单元格数据类型获取数据
        Object value=null;
        switch (type){
            case STRING:
                value=cell.getStringCellValue();
                break;
            case BOOLEAN:
                value=cell.getBooleanCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)){
                    //日期格式
                    value=cell.getDateCellValue();
                }else {
                    //数字
                    value=cell.getNumericCellValue();
                }
                break;
            case FORMULA:
                value=cell.getCellFormula();
                break;
            default:
                break;
        }
        return value;

    }


    /**
     * 测试feign组件
     */
    @GetMapping(value = "/test/{id}")
    public Result test(@PathVariable(value = "id")String id){
        Result result = departmentFeignClient.findById(id);
        return result;
    }


    /**
     * 分配角色
     */
    @RequiresPermissions(value = "USER-ROLE-ASSIGNMENT")
    @PutMapping(value = "/user/assignRoles",name = "USER-ROLE-ASSIGNMENT")
    public Result assignRoles(@RequestBody Map<String,Object> map) {
        //1.获取被分配的用户id
        String userId= (String) map.get("id");
        //2.获取到角色的id列表
        List<String> roleIds=(List<String>) map.get("roleIds");
        //3.调用service完成角色分配
        userService.assignRoles(userId,roleIds);
        return new Result(ResultCode.SUCCESS);
    }


    /**
     * 保存
     */
    @RequiresPermissions(value = "POINT-USER-ADD")
    @PostMapping(value = "/user",name = "POINT-USER-ADD")
    public Result save(@RequestBody User user){
        //1.设置保存的企业id
        user.setCompanyId(companyId);
        user.setCompanyName(companyName);
        //2.调用service保存部门
        userService.save(user);
        //3.构造返回结果
        return new Result(ResultCode.SUCCESS);
    }

    /**
     *查询用户列表
     * 指定企业id
     */
    @GetMapping(value = "/user")
    public Result findAll(int page,int size, @RequestParam Map map){
        //1.获取当前企业id
        map.put("companyId",companyId);
        //2.完成查询
        Page<User> pageUser = userService.findAll(map,page,size);
        //3.构造返回结果
        PageResult pageResult=new PageResult(pageUser.getTotalElements(),pageUser.getContent());
        return new Result(ResultCode.SUCCESS,pageResult);
    }
    /**
     * 根据id查询User
     */
    @GetMapping(value = "/user/{id}")
    public Result findById(@PathVariable(value = "id")String id){
        //添加roleIds（用户已经具有的id数组）
        User user = userService.findById(id);
        UserResult userResult=new UserResult(user);
        return new Result(ResultCode.SUCCESS,userResult);
    }

    /**
     * 修改User
     */
    @RequiresPermissions(value = "POINT-USER-UPDATE")
    @PutMapping(value = "/user/{id}",name = "POINT-USER-UPDATE")
    public Result update(@PathVariable(value = "id")String id, @RequestBody User user){
        //设置修改的部门id
        user.setId(id);
        //2.调用service更新
        userService.update(user);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据id删除
     */
    @RequiresPermissions(value = "API-USER-DELETE")
    @DeleteMapping(value = "/user/{id}",name = "API-USER-DELETE")
    public Result delete(@PathVariable(value = "id")String id){
        userService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 用户登录
     *      1.通过service根据mobile查询用户
     *      2.比较password
     *      3.生成jwt信息
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String,Object>loginMap){
        String mobile =(String)loginMap.get("mobile");
        String password =(String)loginMap.get("password");
        try {
            //1.构造登录令牌 UsernamePasswordToken
            //加密密码,Md5Hash(密码,盐,加密次数)
            password = new Md5Hash(password, mobile, 3).toString();
            UsernamePasswordToken upToken = new UsernamePasswordToken(mobile,password);
            //2.获取Subject
            Subject subject = SecurityUtils.getSubject();
            //3.调用subject的login方法，进入realm完成认证
            subject.login(upToken);
            //4.获取sessionId
            String sessionId=(String) subject.getSession().getId();
            //5.构造返回结果
            return new Result(ResultCode.SUCCESS,sessionId);
        }catch (Exception e){
            return new Result(ResultCode.MOBILEORPASSWORDERROR);
        }




//        User user = userService.findByMobile(mobile);
//            //登录失败
//        if (user == null || !user.getPassword().equals(password)){
//            return new Result(ResultCode.MOBILEORPASSWORDERROR);
//        }else {
//            //登录成功
//            //API权限字符串
//            StringBuilder sb=new StringBuilder();
//            //获取所有可访问API权限
//            for (Role role : user.getRoles()){
//                for (Permission perm : role.getPermissions()) {
//                    if (perm.getType() == PermissionConstants.PERMISSION_API){
//                        sb.append(perm.getCode()).append(",");
//                    }
//                }
//            }
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("apis",sb.toString());
//            map.put("companyId",user.getCompanyId());
//            map.put("companyName",user.getCompanyName());
//            String token = jwtUtils.createJwt(user.getId(), user.getUsername(), map);
//            return new Result(ResultCode.SUCCESS,token);
//        }
    }

    /**
     * 用户登录成功之后获取用户信息
     *      1.获取用户id
     *      2.根据id查询用户
     *      3.构建返回值对象
     *      4.相应数据
     */
    @PostMapping("/profile")
    public Result profile(HttpServletRequest request) throws Exception {

        //获取session安全数据
        Subject subject = SecurityUtils.getSubject();
        //通过subject获取所有的安全数据集合
        PrincipalCollection principals = subject.getPrincipals();
        //获取安全数据
        ProfileResult result = (ProfileResult)principals.getPrimaryPrincipal();


//        String userId=claims.getId();
//        //获取到用户信息
//        User user = userService.findById(userId);
//        //根据不同的用户级别获取用户权限
//
//        ProfileResult result = null;
//
//        if ("user".equals(user.getLevel())){
//            result = new ProfileResult(user);
//        }else {
//            Map map=new HashMap();
//            if ("coAdmin".equals(user.getLevel())){
//                map.put("enVisible","1");
//            }
//            List<Permission> list = permissionService.findAll(map);
//            result = new ProfileResult(user,list);
//        }
        return new Result(ResultCode.SUCCESS,result);
    }

}
