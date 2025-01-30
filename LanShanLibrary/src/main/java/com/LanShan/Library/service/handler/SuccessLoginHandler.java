package com.LanShan.Library.service.handler;

import com.alibaba.fastjson2.JSONObject;
import com.LanShan.Library.pojo.MyUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
//处理登陆成功的返回信息
@Component
public class SuccessLoginHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
    Authentication authentication) throws IOException, ServletException {
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        JSONObject json = new JSONObject();
        json.put("message", "登录成功!");
        json.put("用户名:", userDetails.getUsername());
        json.put("权限:", userDetails.getAuthorities());
        response.getWriter().write(json.toJSONString());
    }
}