package com.LanShan.Library.pojo;

import java.util.Date;
//动态的评论模版
public class Comment {
    public String aim_id;
    public String user_id;
    public String content;
    public Date created_at;
    public String id;

    public String getAim_id() {
        return aim_id;
    }

    public void setAim_id(String aim_id) {
        this.aim_id = aim_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
