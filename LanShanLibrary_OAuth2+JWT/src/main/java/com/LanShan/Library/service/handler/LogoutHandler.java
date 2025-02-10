package com.LanShan.Library.service.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
//登出的handler
@Component
public class LogoutHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
    HttpServletResponse response, Authentication authentication) throws IOException{

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"message\": \"成功登出!\"}");
        response.getWriter().flush();
    }

}