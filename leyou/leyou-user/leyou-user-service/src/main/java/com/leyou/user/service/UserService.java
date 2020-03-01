package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 柒
 * @date 2020-02-27 13:52
 * @Description: 用户操作
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate ;

    @Autowired
    private StringRedisTemplate redisTemplate ;

    // redis中验证码的前缀（文件目录）
    private static final String KEY_PREFIX = "user:code:phone:";

    /**
     * 用户校验
     *
     * @param data
     * @param type 1: 用户名  2: 手机
     * @return
     */
    public Boolean checkUser(String data, Integer type) {
        User record = new User();
        if (type == 1) {
            record.setUsername(data);
        } else if (type == 2) {
            record.setPhone(data);
        } else {
            return null;
        }
        // 用户不存在 返回true 存在返回false
        return this.userMapper.selectCount(record) == 0;
    }

    /**
     * 发送手机验证码
     * @param phone
     */
    public void sendVerifyCode(String phone) {
        // 生成六位验证码
        String code = NumberUtils.generateCode(6);
        // 发送短信
        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        this.amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE", "verifycode.sms", msg);
        // 将信息存入redis
        this.redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5, TimeUnit.MINUTES);
    }

    /**
     * 用户注册
     * @param user
     * @param code
     */
    public void register(User user, String code) {
        // 1. 从redis中查询验证码
        String redis_code = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());

        // 2. 校验验证码
        if(!StringUtils.equals(code, redis_code)){
            return ;
        }

        // 3. 生成salt
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        // 4. 加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        // 5. 存入数据库
        user.setId(null);
        user.setCreated(new Date());
        this.userMapper.insertSelective(user);
        // 6. 删除redis的验证码
        this.redisTemplate.opsForValue().decrement(KEY_PREFIX + user.getPhone());
    }

    /**
     * 查询用户信息
     * @param username
     * @param password
     * @return
     */
    public User queryUser(String username, String password) {
        // 根据用户名查询用户信息
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        if(user == null){
            // 没找到用户
            return  null ;
        }
        // 获取盐 加密密码 和数据库密码进行校验
        String hex = CodecUtils.md5Hex(password, user.getSalt());
        if(StringUtils.equals(hex,user.getPassword())){
            return user ;
        }
        // 密码错误
        return null ;
    }
}
