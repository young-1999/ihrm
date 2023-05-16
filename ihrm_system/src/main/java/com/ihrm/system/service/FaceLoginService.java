package com.ihrm.system.service;


import com.baidu.aip.util.Base64Util;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.FaceLoginResult;
import com.ihrm.domain.system.response.QRCode;
import com.ihrm.system.dao.UserDao;
import com.ihrm.system.utils.BaiduAiUtil;
import com.ihrm.system.utils.QRCodeUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class FaceLoginService {

    @Value("${qr.url}")
    private String url;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private QRCodeUtil qrCodeUtil;

    @Autowired
    private BaiduAiUtil baiduAiUtil;

    @Autowired
    private RedisTemplate redisTemplate;
	//创建二维码
    public QRCode getQRCode() throws Exception {
        //创建唯一标识
        String code=idWorker.nextId()+"";
        //生产二维码
        String content=url+"?code="+code;
        System.out.println(content);
        String file = qrCodeUtil.crateQRCode(content);
        System.out.println(file);
        //存入当前二维码状态
        FaceLoginResult result=new FaceLoginResult("-1");
        redisTemplate.boundValueOps(getCacheKey(code)).set(result,10, TimeUnit.MINUTES);//状态，失效时间，时间单位
		return new QRCode(code,file);
    }

	//根据唯一标识，查询用户是否登录成功
    public FaceLoginResult checkQRCode(String code) {
        String key=getCacheKey(code);
        return (FaceLoginResult)redisTemplate.opsForValue().get(key);
    }

    @Autowired
    private UserDao userDao;

	//扫描二维码之后，使用拍摄照片进行登录
    public String loginByFace(String code, MultipartFile attachment) throws Exception {
        //调用百度云AI查询当前的用户
        String userId = baiduAiUtil.faceSearch(Base64Util.encode(attachment.getBytes()));
        //自动登录（token）
        //1.查询用户对象
        User user= userDao.findById(userId).get();
        FaceLoginResult result=new FaceLoginResult("0");
        if (user !=null){
            //2.获取subject
            Subject subject = SecurityUtils.getSubject();
            //3.调用login方法
            subject.login(new UsernamePasswordToken(user.getMobile(),user.getPassword()));
            //4.获取token（sessionId）
            String token = subject.getSession().getId()+"";
            result=new FaceLoginResult("1",token,userId);
        }
        //修改二维码的状态
        redisTemplate.boundValueOps(getCacheKey(code)).set(result,10,TimeUnit.MINUTES);
		return userId;
    }

	//构造缓存key
    private String getCacheKey(String code) {
        return "qrcode_" + code;
    }
}
