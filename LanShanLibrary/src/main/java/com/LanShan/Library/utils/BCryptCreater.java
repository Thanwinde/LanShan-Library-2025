package com.LanShan.Library.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//调用spring security的BCrypt来加密的工具
public class BCryptCreater {
    public String getBCryptPassword(String password) {
        BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder(4);
        return bcryptPasswordEncoder.encode(password);
    }
}
