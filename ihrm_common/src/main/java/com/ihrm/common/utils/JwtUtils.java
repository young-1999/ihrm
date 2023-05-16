package com.ihrm.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * @Author yao
 * @Description
 * @Date 15:43 2022/12/26
 * @Param null
 * @Return 
**/
@Getter
@Setter
@Component
@ConfigurationProperties("jwt.config")
public class JwtUtils {
    //签名的私钥
    private String key;
    //签名的失效时间
    private Long ttl;

    /**
     * 设置认证token
     *      id:登录用户id
     *      subject：登录用户名
     */
    public String createJwt(String id, String name, Map<String,Object>map){
        //1.设置失效时间
        Long now = System.currentTimeMillis();//当前毫秒数
        Long exp = now + ttl;
        //2.创建jwtBuilder
        JwtBuilder jwtBuilder = Jwts.builder().setId(id)
                .setSubject(name)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, key);
        //3.根据map设置claims
        for (Map.Entry<String,Object>entry:map.entrySet()){
            jwtBuilder.claim(entry.getKey(), entry.getValue());
        }
        jwtBuilder.setExpiration(new Date(exp));
        //4.创建token
        String token = jwtBuilder.compact();
        return token;
    }

    /**
     * 解析token字符串获取claims
     */

    public Claims parseJwt(String token){
        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        return claims;
    }

}
