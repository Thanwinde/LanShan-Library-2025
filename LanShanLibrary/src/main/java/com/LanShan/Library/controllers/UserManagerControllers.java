package com.LanShan.Library.controllers;
import com.LanShan.Library.utils.PasswordChecker;
import com.alibaba.fastjson2.JSONArray;
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

import java.util.List;

@RestController
@RequestMapping("/user")
//用户管理控制器
public class UserManagerControllers {
    @Autowired
    private UserMapper userMapper;

    private PasswordChecker passwordChecker = new PasswordChecker();

    private BCryptCreater creator = new BCryptCreater();

    //管理员添加新用户的接口，有admin权限
    @PostMapping("/add")
    public JSONObject addUser(String username,String password) throws BadRequestException {
        if(username == null || password == null || username.isEmpty() || password.isEmpty())
            throw new BadRequestException("身份信息不可为空，请补齐!");
        User user = userMapper.getUserByUsername(username);
        if(user != null)
            throw new BadRequestException("已经存在同名用户!");
        if(!passwordChecker.checkPassword(password))
            throw new BadRequestException("密码必须由英文字母，数字和标点符号组成，且不小于6位！");
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        String id = IDgeter.generateNextId();
        String row = password;
        password = creator.getBCryptPassword(password);
        String prefix = "{bcrypt}";
        password = prefix + password;
        userMapper.addUser(id,username, password,"admin",true);
        json.put("message","添加管理员成功!");
        json.put("id",id);
        json.put("username",username);
        json.put("password",row);
        json.put("authority","admin");
        return json;
    }

    //一般用户注册接口，只有user权限
    @PostMapping("/register")
    public JSONObject register(String username,String password) throws BadRequestException {
        if(username == null || password == null || username.isEmpty() || password.isEmpty())
            throw new BadRequestException("身份信息不可为空，请补齐!");
        User user = userMapper.getUserByUsername(username);
            if(user != null)
                throw new BadRequestException("已经存在同名用户!");
        if(!passwordChecker.checkPassword(password))
            throw new BadRequestException("密码必须由英文字母，数字和标点符号组成，且不小于6位！");
        JSONObject json = new JSONObject();
        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        String id = IDgeter.generateNextId();
        password = creator.getBCryptPassword(password);
        String prefix = "{bcrypt}";
        password = prefix + password;
        userMapper.addUser(id,username, password,"user",true);
        json.put("message","添加用户成功!");
        json.put("id",id);
        json.put("username",username);
        json.put("password",password);
        json.put("authority","admin");
        return json;
    }

    //删除用户，只有管理员可使用
    @DeleteMapping("/delete")
    public JSONObject deleteUser(@RequestParam List<String> user_id) throws NoResourceFoundException, BadRequestException {
        JSONObject json = new JSONObject();
        int cnt = 0;
        for(String id : user_id) {
            if (id == null || id.isEmpty())
                throw new BadRequestException("id不可为空，请补齐!");
            JSONObject te = new JSONObject();
            User user = userMapper.getUserById(id);
            if(user == null)
                throw new NoResourceFoundException("找不到账户，删除失败！");
            if (userMapper.deleteUser(id) == 0)
                throw new BadRequestException(id + " 删除失败！");
            te.put("id",user.getId());
            te.put("username", user.getUsername());
            json.put("deletedAccount " + cnt, te);
            cnt++;
        }
        return json;
    }

    //获取用户列表
    @GetMapping("/list")
    public JSONObject listUser() {
        JSONObject json = new JSONObject();
        List<User> users= userMapper.getAllUsers();
        JSONArray userArray = new JSONArray();
        for(User user : users){
            JSONObject te = new JSONObject();
            te.put("id",user.getId());
            te.put("username",user.getUsername());
            te.put("introduction",user.getIntroduction());
            te.put("authority",user.getAuthority());
            userArray.add(te);
        }
        json.put("users",userArray);
        return json;
    }

    //查看当前登陆的账户的状态
    @GetMapping("/status")
    public JSONObject getstatus() {
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        json.put("currentAccount: ",authentication);
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
        User user = userMapper.getUserByUsername(username);
        if(user == null)
            throw new NoResourceFoundException("用户不存在！请检查用户名是否正确!");
        if(!passwordChecker.checkPassword(password))
            throw new BadRequestException("密码必须由英文字母，数字和标点符号组成，且不小于6位！");
        String id = userDetails.getId();
        String row = password;
        password = creator.getBCryptPassword(password);
        String prefix = "{bcrypt}";
        password = prefix + password;
        if(user.getId().equals(id) ||userDetails.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            userMapper.updateUser(id,username,password);
        }else
            throw new InsufficientAuthenticationException("无权限更改此内容!");
        json.put("message","成功更改！");
        json.put("id",user.getId());
        json.put("new username",username);
        json.put("new password",row);
        return json;
    }
}
