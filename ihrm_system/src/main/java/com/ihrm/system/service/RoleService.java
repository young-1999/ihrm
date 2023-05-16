package com.ihrm.system.service;

import com.ihrm.common.service.BaseService;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.common.utils.PermissionConstants;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import com.ihrm.system.dao.PermissionDao;
import com.ihrm.system.dao.RoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService extends BaseService {
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private PermissionDao permissionDao;

    /**
     * 分配权限
     */
    public void assignPerms(String roleId, List<String> permIds){
        //1.获取分配的角色对象
        Role role = roleDao.findById(roleId).get();
        //2.构造角色的权限集合
        Set<Permission> perms=new HashSet<>();
        for (String permId : permIds){
            Permission permission = permissionDao.findById(permId).get();
            //需要根据父id和类型查询API权限列表
            List<Permission> apiList = permissionDao.findByTypeAndPid(PermissionConstants.PERMISSION_API, permission.getId());
            perms.addAll(apiList);//自动赋予api权限
            perms.add(permission);//当前菜单或按钮的权限
        }
        //3.设置角色和权限的关系
        role.setPermissions(perms);
        //4.更新角色
        roleDao.save(role);
    }

    /**
     * 新增角色
     */
    public void save(Role role){
        String id = idWorker.nextId() + "";
        role.setId(id);
        roleDao.save(role);
    }

    /**
     * 根据id删除角色
     */
    public void deleteById(String id){
        roleDao.deleteById(id);
    }
    /**
     * 修改角色
     */
    public void update(Role role){
        Role target = roleDao.getOne(role.getId());
        target.setName(role.getName());
        target.setDescription(role.getDescription());
        roleDao.save(target);
    }

    /**
     * 根据id查询角色
     */
    public Role findById(String id){
        Role role = roleDao.findById(id).get();
        return role;
    }

    /**
     * 查询企业所有角色
     */
    public Page<Role> findSearch(String companyId,int page,Integer size){

        return roleDao.findAll(getSpec(companyId),PageRequest.of(page-1,size));
    }

    public List<Role> findAll(String companyId) {
        return roleDao.findAll(getSpec(companyId));
    }






}
