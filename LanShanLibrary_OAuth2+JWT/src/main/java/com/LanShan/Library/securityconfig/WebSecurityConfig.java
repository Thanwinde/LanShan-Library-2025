package com.LanShan.Library.securityconfig;

import com.LanShan.Library.controllers.UserManagerControllers;
import com.LanShan.Library.service.handler.*;
import com.LanShan.Library.utils.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

//配置SpringSecurity
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Autowired
    private DBUserDetailsManager userDetailsManager;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserManagerControllers userManagerControllers;
    @Bean
    public UserDetailsService userDetailsService() {
        DBUserDetailsManager manager = new DBUserDetailsManager();
        return manager;
    }//把默认的UserDetailsService设成自定义的Manager来管理

    @Bean
    public PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder(4);
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }//设置密码解码方式

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsManager);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }//指定AuthenticationProvider的认证方式与密码

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }//可以不写，未来规范还是写一下

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //设置过滤器链
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/user/add").hasAuthority("root")
                        .requestMatchers("/user/delete","/user/editaccount","/book/add","/book/delbook","/book/update").hasAuthority("admin")//需要管理员的接口
                        .requestMatchers("/","/login", "/user/register","/user/emailregister","/user/emailregister/**","/user/emaillogin/**").permitAll()//不拦截
                        .anyRequest().authenticated()
                )
                .formLogin( form -> {
                    form
                            .loginPage("/login") //登录页面无需授权即可访问
                            .loginProcessingUrl("/dologin")
                            .usernameParameter("username") //表单用户名参数，默认就是这个，可以自己改
                            .passwordParameter("password") //表单密码参数
                            .successHandler(new SuccessLoginHandler(jwtService))//登录成功、失败的handler
                            .failureHandler(new FailLoginHandler());
                }); //使用表单授权方式
                //.httpBasic(withDefaults()); // 基本授权方式
        http.exceptionHandling(exception  -> {
            exception.accessDeniedHandler(new MyaccessDeniedHandler());//权限不够的handler
            exception.authenticationEntryPoint(new AuthenticationEntryPointHandler());//请求未认证的接口
        });
        http.logout(logout -> {
            logout.logoutUrl("/logout");
            logout.logoutSuccessHandler(new LogoutHandler()); //登出的handler
        });
        http.csrf(AbstractHttpConfigurer::disable);//关闭csrf防御，无前端不好实现
        http.cors(withDefaults());//不允许跨域请求
        http.sessionManagement(session -> {
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);//关闭session
        });
        http.authenticationProvider(authenticationProvider()).addFilterBefore(
                jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http
                .oauth2Client(Customizer.withDefaults());
        http
                .oauth2Login(oauth2->oauth2.successHandler(new GithubSuccessLoginHandler(jwtService,userManagerControllers)));

        return http.build();
    }
}