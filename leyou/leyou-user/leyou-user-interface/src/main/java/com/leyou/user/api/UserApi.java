package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 柒
 * @date 2020-02-28 19:09
 * @Description: user服务对外提供的接口
 */
public interface UserApi {

    /**
     * 根据用户名和密码查询用户信息
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public User queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    );
}
