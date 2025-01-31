package com.LanShan.Library.controllers;
import com.alibaba.fastjson2.JSONObject;
import com.LanShan.Library.mapper.UserMapper;
import com.LanShan.Library.pojo.MyUserDetails;
import com.LanShan.Library.pojo.User;
import com.LanShan.Library.utils.BCryptCreater;

import com.LanShan.Library.utils.SnowFlakeGenerator;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.resource.NoResourceFoundException;

@RestController
@RequestMapping("/user")
//用户管理控制器
public class UserManagerControllers {
    @Autowired
    private UserMapper userMapper;

    private BCryptCreater creater = new BCryptCreater();

    //管理员添加新用户的接口，有admin权限
    @PostMapping("/add")
    public JSONObject addUser(String username,String password) throws BadRequestException {
        if(username == null || password == null || username.isEmpty() || password.isEmpty())
            throw new BadRequestException("身份信息不可为空，请补齐!");
        User user = userMapper.GetUserByUsername(username);
        if(user != null)
            throw new BadRequestException("已经存在同名用户!");
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        String id = IDgeter.generateNextId();
        password = creater.getBCryptPassword(password);
        String prefix = "{bcrypt}";
        password = prefix + password;
        userMapper.AddUser(id,username, password,"admin",true);
        json.put("user",userMapper.GetUserById(id));
        return json;
    }

    //一般用户注册接口，只有user权限
    @PostMapping("/register")
    public JSONObject register(String username,String password) throws BadRequestException {
        if(username == null || password == null || username.isEmpty() || password.isEmpty())
            throw new BadRequestException("身份信息不可为空，请补齐!");
        User user = userMapper.GetUserByUsername(username);
            if(user != null)
                throw new BadRequestException("已经存在同名用户!");
        JSONObject json = new JSONObject();
        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        String id = IDgeter.generateNextId();
        password = creater.getBCryptPassword(password);
        String prefix = "{bcrypt}";
        password = prefix + password;
        userMapper.AddUser(id,username, password,"user",true);
        json.put("user",userMapper.GetUserById(id));
        return json;
    }

    //删除用户，只有管理员可使用
    @DeleteMapping("/delete")
    public JSONObject deleteUser(@RequestParam String user_id) throws NoResourceFoundException, BadRequestException {
        if(user_id == null || user_id.isEmpty())
            throw new BadRequestException("id不可为空，请补齐!");
        JSONObject json = new JSONObject();
        if(userMapper.DeleteUser(user_id) == 0)
            throw new NoResourceFoundException("找不到账户，删除失败！");
        json.put("message","已删除" + user_id);
        return json;
    }

    //获取用户列表，根据是不是管理员决定给不给出密码
    @GetMapping("/list")
    public JSONObject listUser() {
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> "admin".equals(authority.getAuthority()))) {
            json.put("userlist",userMapper.AGetAllUsers());
        }else
            json.put("userlist",userMapper.GetAllUsers());
        return json;
    }

    //查看当前登陆的账户的状态
    @GetMapping("/status")
    public JSONObject getstatus() {
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        json.put("Account:",authentication);
        return json;
    }

    //更新用户名或密码，管理员能更改全部，其余只能更改自己的
    @PutMapping("/account")
    public JSONObject account(@RequestParam String username,@RequestParam String password) throws BadRequestException {
        if(username == null || password == null || username.isEmpty() || password.isEmpty())
            throw new BadRequestException("身份不可为空，请补齐!");
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        User user = userMapper.GetUserByUsername(username);
        String id = userDetails.getId();
        password = creater.getBCryptPassword(password);
        String prefix = "{bcrypt}";
        password = prefix + password;
        if(user.getId().equals(id) ||userDetails.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            userMapper.UpdateUser(id,username,password);
        }else
            throw new InsufficientAuthenticationException("无权限更改此内容!");
        json.put("message","成功更改！");
        json.put("user",user);
        return json;
    }
}
