package com.LanShan.Library.pojo;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

//用户信息模版
public class MyUserDetails implements UserDetails {


    private String username;
    private String password;
    private String email;
    private Collection<? extends GrantedAuthority> authorities;
    private String id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyUserDetails that = (MyUserDetails) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MyUserDetails{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", authorities=" + authorities +
                ", email='" + email + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    public MyUserDetails(String username, String password, String id,String email, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.email = email;
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
