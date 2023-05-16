package com.ihrm.system.service;

import com.baidu.aip.util.Base64Util;
import com.ihrm.common.service.BaseService;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.company.Department;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import com.ihrm.system.client.DepartmentFeignClient;
import com.ihrm.system.dao.RoleDao;
import com.ihrm.system.dao.UserDao;
import com.ihrm.system.utils.BaiduAiUtil;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.*;
/**
 * @Author yao
 * @Description
 * @Date 14:03 2023/3/10
 * @Param null
 * @Return
**/

@Service
public class UserService extends BaseService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private DepartmentFeignClient departmentFeignClient;

    /**
     * 批量保存用户
     */
    @Transactional
    public void saveAll(List<User> list,String companyId,String companyName){

        for (User user : list) {
            //默认密码
            user.setPassword(new Md5Hash("123456",user.getMobile(),3).toString());
            //设置id
            user.setId(new IdWorker().nextId()+"");
            //基本属性
            user.setCompanyId(companyId);
            user.setCompanyName(companyName);
            user.setInServiceStatus(1);
            user.setEnableState(1);
            user.setLevel("user");
            //添加部门信息
            Department dept = departmentFeignClient.findById(user.getDepartmentId(), user.getCompanyId());
            if (dept!=null){
                user.setDepartmentId(dept.getId());
                user.setDepartmentName(dept.getName());
            }
            userDao.save(user);
        }
    }

    /**
     * 1.保存用户
     */
    public void save(User user){
        //设置主键值
        String id = idWorker.nextId() + "";
        String password = new Md5Hash("123456", user.getMobile(), 3).toString();
        user.setPassword(password);//设置用户初始密码
        user.setEnableState(1);
        user.setCreateTime(new Date());
        user.setId(id);
        user.setLevel("user");
        //调用dao保存用户
        userDao.save(user);
    }

    /**
     * 更新用户
     */
    public void update(User user){
        //1.根据id查询用户
        User target = userDao.findById(user.getId()).get();
        //2.设置部门属性
        target.setUsername(user.getUsername());
        target.setPassword(user.getPassword());
        target.setDepartmentId(user.getDepartmentId());
        target.setDepartmentName(user.getDepartmentName());
        //3.更新用户
        userDao.save(target);
    }

    /**
     * 根据id查询用户
     */
    public User findById(String id){
        return userDao.findById(id).get();
    }

    public List<User> findAll(String companyId) {
        return userDao.findAll(super.getSpec(companyId));
    }

    /**
     * 4.查询全部用户列表
     *      参数：map集合的形式
     *          hasDept
     *          departmentId
     *          companyId
     */
    public Page findAll(Map<String,Object> map,int page,int size){
        //1.需要查询条件
        Specification<User> spec=new Specification<User>() {
            /**
             * 动态拼接查询条件
             *
             * @return
             */
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list=new ArrayList<>();
                //根据请求的companyId是否为空构造查询条件
                if (!StringUtils.isEmpty(map.get("companyId"))){
                    list.add(cb.equal(root.get("companyId").as(String.class),(String)map.get("companyId")));
                }
                //根据请求的departmentId是否为空构造查询条件
                if (!StringUtils.isEmpty(map.get("departmentId"))){
                    list.add(cb.equal(root.get("departmentId").as(String.class),(String)map.get("departmentId")));
                }
                if (!StringUtils.isEmpty(map.get("hasDept"))){
                    //根据请求的hasDept是否为空构造查询条件
                    // 是否分配部门 0 未分配（departmentId==null），1 已分配（departmentId！=null）
                    if ("0".equals((String) map.get("hasDept"))){
                        list.add(cb.isNull(root.get("departmentId")));
                    }else {
                        list.add(cb.isNotNull(root.get("departmentId")));
                    }
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        //2.分页
        Page<User> userPage = userDao.findAll(spec,new PageRequest(page-1,size));
        return userPage;
    }
    /**
     * 根据id删除用户
     */
    public void deleteById(String id){
        userDao.deleteById(id);
    }

    /**
     * 分配角色
     */
    public void assignRoles(String userId,List<String> roleIds){
        //1.根据id查询用户
        User user = userDao.findById(userId).get();
        //2.设置用户的角色集合
        Set<Role> roles=new HashSet<>();
        for (String roleId : roleIds){
            Role role = roleDao.findById(roleId).get();
            roles.add(role);
        }
        //设置用户和角色集合之间的关系
        user.setRoles(roles);
        //3.更新用户
        userDao.save(user);
    }

    /**
     * 根据mobile查询用户
     */
    public User findByMobile(String mobile){
        return userDao.findByMobile(mobile);
    }


    @Autowired
    private BaiduAiUtil baiduAiUtil;
    /**
     * 图片处理
     * @param id
     * @param file
     * @return
     */
    public String uploadImage(String id, MultipartFile file) throws IOException {
        //根据id查询用户
        User user = userDao.findById(id).get();
        //使用dataUrl存储图片
        String encode = "data:image/png;base64,"+Base64.encode(file.getBytes());
        //更新用户头像地址
        user.setStaffPhoto(encode);
        userDao.save(user);
        //判断是否已经注册面部信息
        Boolean aBoolean = baiduAiUtil.faceExist(id);
        String base64= Base64Util.encode(file.getBytes());
        if (aBoolean){
            //更新
            baiduAiUtil.faceUpdate(id,base64);
        }else {
            //注册
            baiduAiUtil.faceRegister(id,base64);
        }
        //返回
        return encode;
    }
}
