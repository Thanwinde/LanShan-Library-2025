package com.LanShan.Library.securityconfig;

import com.LanShan.Library.utils.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
//代替原有session过滤器的jwt过滤器
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;
    @Autowired
    private DBUserDetailsManager userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");//获取jwt
        final String jwt;
        final String username;

        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 去掉 Bearer 前缀）
        jwt = authHeader.substring(7);

        // 从JWT中提取用户名
        username = jwtService.extractUserName(jwt);

        // 用户存在，但没有认证信息
        if (StringUtils.isNotEmpty(username)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 加载用户信息
            UserDetails userDetails = userService.loadUserByUsername(username);

            // 验证JWT是否有效
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 创建一个新的认证上下文
                SecurityContext context = SecurityContextHolder.createEmptyContext();

                // 创建Token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                //用了JWT就没必要在里面加入密码了

                // 加入IP等信息
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 存储认证上下文
                context.setAuthentication(authToken);

                // 将认证上下文保存到SecurityContextHolder
                SecurityContextHolder.setContext(context);
            }
        }

        // 继续到下一个过滤器
        filterChain.doFilter(request, response);
    }
}
