package com.leyou.cart.interceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 柒
 * @date 2020-02-29 14:54
 * @Description: 登录拦截器
 * ctrl o : 重写父类方法
 */
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    /* 定义一个线程域，存放登录用户 */
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties ;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取cookie
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());

        // 解析jwt
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
        if(userInfo == null){
            return false ;
        }
        // 放入到线程域中
        THREAD_LOCAL.set(userInfo);

        return true ;
    }

    /**
     * 返回userInfo
     * @return
     */
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    /**
     * 方法执行完之后清除线程域
     * 由于web采用的是tomcat的线程池，一个线程执行完后不会结束而是会返回到线程池中，这样当下一个线程分配到该线程时，线程域中的数据就不准确了
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }
}
