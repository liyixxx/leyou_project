package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * @author 柒
 * @date 2020-02-27 13:47
 * @Description: 用户实体类
 */
@Table(name = "tb_user")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Length(min = 4, max = 10 ,message = "用户名必须在4-30位之间")
    private String username;// 用户名

    @JsonIgnore // 对象序列化为json字符串时 忽略该属性
    @Length(min = 4, max = 10 ,message = "用户名必须在4-30位之间")
    private String password;// 密码

//    @Email(message = "邮箱格式错误！")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$",message = "邮箱格式错误！")
    private String phone;// 电话(改成邮箱了 ，电话接收不了短信验证信息)

    private Date created;// 创建时间

    @JsonIgnore
    private String salt;// 密码的盐值

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}