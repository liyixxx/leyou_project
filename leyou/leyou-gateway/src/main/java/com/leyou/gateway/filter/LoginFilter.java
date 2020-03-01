package com.leyou.gateway.filter;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 柒
 * @date 2020-02-28 20:53
 * @Description:
 */
@Component
public class LoginFilter extends ZuulFilter{

    @Autowired
    private JwtProperties jwtProperties ;

    @Autowired
    private FilterProperties filterProperties ;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        // 如果请求在白名单中，则放行
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String url = request.getRequestURL().toString();
        List<String> allowPaths = this.filterProperties.getAllowPaths();
        for (String path : allowPaths) {
            if(StringUtils.contains(url,path)){
                return false ;
            }
        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        // 获取zuul上下文
        RequestContext context = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest request = context.getRequest();
        // 获取cookie
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        if(StringUtils.isBlank(token)){
            // 拦截请求
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        // 解析cookie
        try {
            JwtUtils.getInfoFromToken(token,this.jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }
}
