package com.ihrm.common.feign;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class FeignConfiguration {
    //配置拦截器解决请求头问题
    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            //获取所有浏览器发送的请求属性，请求头复制到feign
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //获取所有请求属性
                ServletRequestAttributes attributes =(ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                if (attributes!=null){
                    HttpServletRequest request = attributes.getRequest();
                    //获取浏览器发起的请求头
                    Enumeration<String> headerNames = request.getHeaderNames();
                    if (headerNames != null){
                        while (headerNames.hasMoreElements()){
                            String name= headerNames.nextElement();//请求头名称：Authorization
                            String value=request.getHeader(name);//根据请求头名称获取数据
                            requestTemplate.header(name,value);
                        }
                    }
                }

            }
        };
    }

}
