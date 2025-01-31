package com.LanShan.Library.pojo;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

//用户信息模版
public class MyUserDetails implements UserDetails {


    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private String id;


    public MyUserDetails(String username, String password, String id,Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.id = id;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }//账户是否过期

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }//是否锁住

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }//密码是否过期

    @Override
    public boolean isEnabled() {
        return true;
    }//是否启用
}
