package com.leyou.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.client.AuthClient;
import com.leyou.config.JwtProperties;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 柒
 * @date 2020-02-28 18:47
 * @Description:
 */
@Service
public class AuthService {

    @Autowired
    private JwtProperties jwtProperties ;

    @Autowired
    private AuthClient authClient ;

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    public String accredit(String username, String password) {
        // 查询user，判断是否为null
        User user = this.authClient.queryUser(username, password);
        if(user == null){
            return null ;
        }
        // 设置jwt
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            return JwtUtils.generateToken(userInfo,this.jwtProperties.getPrivateKey(),this.jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null ;
    }
}
