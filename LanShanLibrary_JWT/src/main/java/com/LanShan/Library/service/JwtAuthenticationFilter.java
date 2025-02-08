package com.LanShan.Library.service;

import com.LanShan.Library.utils.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

@Component  // 组件标识符，Spring会自动扫描并注册为Bean
@RequiredArgsConstructor  // 通过构造器注入依赖
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;  // 注入JwtService，用于JWT处理
    @Autowired
    private DBUserDetailsManager userService;  // 注入自定义的用户服务，用于加载用户信息

    // 过滤器的核心方法，在每个请求的处理链中执行
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头中获取Authorization字段，通常存储JWT
        final String authHeader = request.getHeader("Authorization");
        final String jwt;  // JWT令牌
        final String username;  // 存储从JWT中提取的用户邮箱

        // 如果Authorization头不存在或格式不正确，直接继续处理请求
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取JWT令牌（去掉"Bearer "前缀）
        jwt = authHeader.substring(7);

        // 从JWT中提取用户名（假设JWT中包含email作为用户名）
        username = jwtService.extractUserName(jwt);

        // 如果用户邮箱存在，并且当前没有认证信息，进行验证和认证
        if (StringUtils.isNotEmpty(username)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 根据用户邮箱加载用户详细信息
            UserDetails userDetails = userService.loadUserByUsername(username);

            // 验证JWT是否有效
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 创建一个新的认证上下文
                SecurityContext context = SecurityContextHolder.createEmptyContext();

                // 创建一个包含用户信息的认证令牌
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // 设置额外的身份验证信息
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置认证上下文
                context.setAuthentication(authToken);

                // 将认证上下文保存到SecurityContextHolder
                SecurityContextHolder.setContext(context);
            }
        }

        // 继续处理请求
        filterChain.doFilter(request, response);
    }
}
