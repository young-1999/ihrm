package com.ihrm.system.shiro.realm;

import com.ihrm.common.shiro.realm.IhrmRealm;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.ProfileResult;
import com.ihrm.system.service.PermissionService;
import com.ihrm.system.service.UserService;
import org.apache.shiro.authc.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRealm extends IhrmRealm {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //1.获取用户手机号和密码
        UsernamePasswordToken upToken=(UsernamePasswordToken)authenticationToken;
        String mobile = upToken.getUsername();
        String password=new String(upToken.getPassword());
        //2.根据手机号查询用户
        User user = userService.findByMobile(mobile);
        //3.判断用户是否存在，账号和密码是否一致
        if (user!=null && user.getPassword().equals(password)){
            //如果一致返回安全数据
            //4.构造安全数据并返回（安全数据：用户基本数据，权限信息profileResult）
            //构造方法，安全数据，密码，realm域名
            ProfileResult result=null;
            if ("user".equals(user.getLevel())){
                result = new ProfileResult(user);
            }else {
                Map map=new HashMap();
                if ("coAdmin".equals(user.getLevel())){
                    map.put("enVisible","1");
                }
                List<Permission> list = permissionService.findAll(map);
                result = new ProfileResult(user,list);
            }

            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(result, user.getPassword(), this.getName());
            return info;
        }

        //返回null会抛出异常，标识用户名或密码错误
        return null;
    }
}
