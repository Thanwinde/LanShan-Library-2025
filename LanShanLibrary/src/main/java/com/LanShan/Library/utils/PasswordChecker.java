package com.LanShan.Library.utils;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
public class PasswordChecker {
    public boolean checkPassword(String password) {
        //正则表达式，密码必须包含英文字母、数字和标点符号，且长度不小于6位
        String regex = "^[a-zA-Z0-9\\p{Punct}]{6,}$";
        return password != null && Pattern.matches(regex, password);
    }
}
