package com.LanShan.Library.service.handler;

import com.LanShan.Library.utils.JWTService;
import com.alibaba.fastjson2.JSONObject;
import com.LanShan.Library.pojo.MyUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
//处理登陆成功地返回信息
@Component
public class SuccessLoginHandler implements AuthenticationSuccessHandler {
    public final JWTService jwtService;
    public SuccessLoginHandler(JWTService jwtService) {
        this.jwtService = jwtService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
    Authentication authentication) throws IOException, ServletException {
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        JSONObject json = new JSONObject();
        json.put("message", "登录成功!");
        json.put("ID", userDetails.getId());
        json.put("用户名:", userDetails.getUsername());
        json.put("权限:", userDetails.getAuthorities());
        json.put("JWT", jwtService.generateToken(userDetails));
        json.put("tip", "初次登录密码为123456");
        response.getWriter().write(json.toJSONString());
    }
}