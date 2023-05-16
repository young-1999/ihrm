package com.ihrm.system.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.response.RoleResult;
import com.ihrm.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//1.解决跨域
@CrossOrigin
//2.声明restController
@RestController
//3.设置父路径
@RequestMapping("/sys")
public class RoleController extends BaseController {
    @Autowired
    private RoleService roleService;

    /**
     * 分配权限
     */
    @PutMapping(value = "/role/assignPrem")
    public Result assignPrem(@RequestBody Map<String,Object> map) {
        //1.获取被分配的角色id
        String roleId= (String) map.get("id");
        //2.获取到权限的id列表
        List<String> permIds=(List<String>) map.get("permIds");
        //3.调用service完成权限分配
        roleService.assignPerms(roleId,permIds);
        return new Result(ResultCode.SUCCESS);
    }

    //新增角色
    @PostMapping("/role")
    public Result add(@RequestBody Role role){
        role.setCompanyId(companyId);
        roleService.save(role);
        return new Result(ResultCode.SUCCESS);
    }

    //根据id删除角色
    @DeleteMapping("/role/{id}")
    public Result delete(@PathVariable(value = "id") String id){
        roleService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }

    //修改角色
    @PutMapping("/role/{id}")
    public Result update(@PathVariable(value = "id")String id,@RequestBody Role role){
        role.setId(id);
        roleService.update(role);
        return new Result(ResultCode.SUCCESS);
    }

    //根据id获取角色
    @GetMapping("/role/{id}")
    public Result findById(@PathVariable(value = "id") String id){
        Role role = roleService.findById(id);
        RoleResult roleResult=new RoleResult(role);
        return new Result(ResultCode.SUCCESS,roleResult);
    }

    //分页查询角色
    @GetMapping("/role")
    public Result findByPage(Integer page,Integer pagesize){
        Page<Role> rolePage = roleService.findSearch(companyId, page, pagesize);
        PageResult<Role> pr = new PageResult(rolePage.getTotalElements(), rolePage.getContent());
        return new Result(ResultCode.SUCCESS,pr);
    }

    @RequestMapping(value="/role/list" ,method=RequestMethod.GET)
    public Result findAll() throws Exception {
        List<Role> roleList = roleService.findAll(companyId);
        return new Result(ResultCode.SUCCESS,roleList);
    }
}
