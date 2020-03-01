package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

/**
 * @author 柒
 * @date 2020-02-27 13:53
 * @Description:
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService ;

    /**
     * 用户校验
     * @param data 要校验的数据
     * @param type 校验的数据类型: 1=用户名 2=手机号
     * @return 需要返回一个Boolean值
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUser(
            @PathVariable("data") String data ,
            @PathVariable("type") Integer type
    ){
        Boolean flag = this.userService.checkUser(data,type);
        // 参数不和法时 返回错误
        if(flag == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(flag);
    }

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone") String phone){
        this.userService.sendVerifyCode(phone);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void>  register(@Valid User user, @RequestParam("code") String code){
        this.userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户信息 结果不包含密码和盐值
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ){
        User user = this.userService.queryUser(username,password);
        if(user == null){
            // 用户名或密码错误
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);
    }
}
