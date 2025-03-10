package com.LanShan.Library.service.handler;

import com.LanShan.Library.pojo.User;
import com.LanShan.Library.securityconfig.DBUserDetailsManager;
import com.LanShan.Library.utils.JWTService;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class EmailLoginHandler {

    @Autowired
    private DBUserDetailsManager userService;

    @Autowired
    JWTService jwtService;

    public JSONObject emaillogin(HttpServletRequest request, HttpServletResponse response, User user) {
        JSONObject json = new JSONObject();
        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
        //HttpSession session = request.getSession(true);
        //session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        json.put("message","邮箱登录成功！");
        json.put("username",user.getUsername());
        json.put("email",user.getEmail());
        json.put("authority",user.getAuthority());
        json.put("JWT", jwtService.generateToken(userDetails));
        return json;
    }
}
