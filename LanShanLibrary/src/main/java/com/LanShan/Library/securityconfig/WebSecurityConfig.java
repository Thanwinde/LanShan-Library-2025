package com.LanShan.Library.securityconfig;

import com.LanShan.Library.service.DBUserDetailsManager;
import com.LanShan.Library.service.handler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

//配置SpringSecurity
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        DBUserDetailsManager manager = new DBUserDetailsManager();
        return manager;
    }//把默认的UserDetailsService设成自定义的Manager来管理

    @Bean
    public PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder(10);
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }//设置密码解码方式，可以自己解码也可直接用内置判断

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //设置过滤器链
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/user/add","/user/delete","/book/add","/book/delbook","/book/update").hasAuthority("admin")//需要管理员的接口
                        .requestMatchers("/user/list").hasAnyAuthority("admin", "user")//有权限即可（可以在api中实现）
                        .requestMatchers("/login", "/user/register").permitAll()//这两个不拦截
                        .anyRequest().authenticated()

                )
                .formLogin( form -> {
                    form
                            .loginPage("/login") //登录页面无需授权即可访问
                            .usernameParameter("username") //表单用户名参数，默认就是这个，可以自己改
                            .passwordParameter("password") //表单密码参数
                            .successHandler(new SuccessLoginHandler())//登录成功、失败的handler
                            .failureHandler(new FailLoginHandler());
                }); //使用表单授权方式
                //.httpBasic(withDefaults()); // 基本授权方式
                //采用基本就会给你一个spring security的页面
        http.exceptionHandling(exception  -> {
            exception.accessDeniedHandler(new MyaccessDeniedHandler());//权限不够的handler
            exception.authenticationEntryPoint(new AuthenticationEntryPointHandler());//请求未认证的接口
        });
        http.logout(logout -> {
            logout.logoutSuccessHandler(new LogoutHandler()); //登出的handler
        });
        http.csrf(AbstractHttpConfigurer::disable);//关闭csrf防御（打开的话需要请求里带有csrf令牌，调试麻烦，后面有需求自行打开
        http.cors(withDefaults());//允许跨域请求
        http.sessionManagement(session -> {
            session
                    .maximumSessions(1)//一个账户只能一个用户登录
                    .expiredSessionStrategy(new MultSessionInformationExpiredStrategy());
        });
        return http.build();
    }
}