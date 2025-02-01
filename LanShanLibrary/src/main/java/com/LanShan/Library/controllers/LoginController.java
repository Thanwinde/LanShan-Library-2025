package com.LanShan.Library.controllers;

import com.LanShan.Library.pojo.MyUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class LoginController {

    //登录接口
    @GetMapping({"/","/login"})
    public String getlogin() throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.getPrincipal() instanceof MyUserDetails) {
            throw new BadRequestException("你已登陆！请勿重复登陆！");
        }
        return "login";
    }

    @PostMapping({"/","/login"})
    public void postlogin(@RequestParam String username, @RequestParam String password,  HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.getPrincipal() instanceof MyUserDetails) {
            throw new BadRequestException("你已登陆！请勿重复登陆！");
        }
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BadRequestException("用户名和密码不能为空！");
        }
        request.setAttribute("username", username);
        request.setAttribute("password", password);
        request.getRequestDispatcher("/dologin").forward(request, response);
        return;
    }

}