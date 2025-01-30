package com.LanShan.Library.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    //登录接口，万物起源
    @GetMapping("/login")
    public String login() {
        return "login";
    }

}