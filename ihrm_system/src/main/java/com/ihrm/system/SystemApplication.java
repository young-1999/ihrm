package com.ihrm.system;


import com.ihrm.common.utils.IdWorker;
import com.ihrm.common.utils.JwtUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

//配置springboot包扫描
@SpringBootApplication(scanBasePackages ="com.ihrm")
//配置Jpa注解扫描
@EntityScan(value ="com.ihrm.domain.system")
//注册到eureka
@EnableEurekaClient
@EnableDiscoveryClient
@EnableFeignClients
public class SystemApplication {
    /**
     * 配置启动方法
     */
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class,args);
    }

    @Bean
    public IdWorker idWorker(){
        return new IdWorker();
    }

    @Bean
    public JwtUtils jwtUtils(){
        return new JwtUtils();
    }
    //解决no session问题
    @Bean
    public OpenEntityManagerInViewFilter openEntityManagerInViewFilter(){
        return new OpenEntityManagerInViewFilter();
    }
}
