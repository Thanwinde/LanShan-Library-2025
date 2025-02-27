package com.LanShan.Library.service.handler;

import com.LanShan.Library.controllers.UserManagerControllers;
import com.LanShan.Library.pojo.MyUserDetails;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
public class GithubSuccessLoginHandler implements AuthenticationSuccessHandler {
    public GithubSuccessLoginHandler(UserManagerControllers controllers) {
        this.controllers = controllers;
    }
    final
    UserManagerControllers controllers;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        Map<String, Object> attributes = oauth2User.getAttributes();

        String id = oauth2User.getName();
        String login = (String) attributes.get("login");
        controllers.githubregister(id, login);
        MyUserDetails userDetails = new MyUserDetails(login,"123456",id,null,Collections.singleton(new SimpleGrantedAuthority("user")));
        JSONObject json = new JSONObject();
        json.put("message", "github登录成功!");
        json.put("ID", userDetails.getId());
        json.put("用户名:", userDetails.getUsername());
        json.put("权限:", userDetails.getAuthorities());
        json.put("tip","第一次登录初始密码为123456");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(json.toJSONString());
    }
}