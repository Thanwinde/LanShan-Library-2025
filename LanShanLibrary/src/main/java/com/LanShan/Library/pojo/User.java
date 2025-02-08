package com.LanShan.Library.pojo;
//用户信息模版
public class User {
    private String id;

    private String username;

    private String password;

    private String authority;

    private Boolean enabled;

    private String introduction;

    private Integer max_borrow;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Integer getMax_borrow() {
        return max_borrow;
    }

    public void setMax_borrow(Integer max_borrow) {
        this.max_borrow = max_borrow;
    }
}