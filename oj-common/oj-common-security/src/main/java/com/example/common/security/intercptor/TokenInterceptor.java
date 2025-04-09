package com.example.common.security.intercptor;

import com.example.common.security.service.TokenService;
import com.example.core.constants.HttpConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//这个拦截器是绑定在具体的某个微服务上的 并非网关 每个微服务都绑定这个拦截器 这个bean在引用的微服务中都有
//注意拦截器还需要在WebMvcConfiguration中挂载
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Value("${jwt.secret}") //从哪个地方拿 取决于这个bean是在哪个微服务管理
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        //已经在filter中经过了身份校验 获取token
        tokenService.extendToken(getToken(request),secret);
        return true;
    }

    private static String getToken(HttpServletRequest request) {
        String token = request.getHeader(HttpConstants.AUTHENTICATION);
        token = token.replaceFirst(HttpConstants.AUTHENTICATION_PREFIX, "");
        return token;
    }

}
