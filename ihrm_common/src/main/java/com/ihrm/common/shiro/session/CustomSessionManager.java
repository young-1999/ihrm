package com.ihrm.common.shiro.session;

import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * @Author yao
 * @Description
 * @Date 21:01 2023/3/6
 * @Param null
 * @Return 
**/
public class CustomSessionManager extends DefaultWebSessionManager {



    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response){

        //获取请求头Authorization中的数据
        String id = WebUtils.toHttp(request).getHeader("Authorization");
        if (StringUtils.isEmpty(id)){
            //如果没有携带生产新的sessionId
            return super.getSessionId(request,response);
        }else {
            //请求头信息：bearer sessionId
            id =id.replaceAll("Bearer ", "");
            //返回sessionId
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,"header");
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID,id);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID,Boolean.TRUE);
            return id;
        }
    }

}
