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

    private final PasswordChecker passwordChecker = new PasswordChecker();

    private final BCryptCreater creator = new BCryptCreater();

    //添加新管理员的接口，root才可使用
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
        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        String id = IDgeter.generateNextId();
        String row = password;
        password = creator.getBCryptPassword(password);
        String prefix = "{bcrypt}";
        password = prefix + password;
        userMapper.addUser(id,username, password,"admin",true,10);
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
        String row = password;
        password = creator.getBCryptPassword(password);
        String prefix = "{bcrypt}";
        password = prefix + password;
        userMapper.addUser(id,username, password,"user",true,3);
        json.put("message","添加用户成功!");
        json.put("id",id);
        json.put("username",username);
        json.put("password",row);
        json.put("authority","user");
        return json;
    }

    //删除用户，只有管理员可使用，root才可删除管理员账户
    @DeleteMapping("/delete")
    public JSONObject deleteUser(@RequestParam List<String> user_id) throws NoResourceFoundException, BadRequestException {
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        int cnt = 0;
        for(String id : user_id) {
            if (id == null || id.isEmpty())
                throw new BadRequestException("id不可为空，请补齐!");
            JSONObject te = new JSONObject();
            User user = userMapper.getUserById(id);
            if(user == null)
                throw new NoResourceFoundException("找不到账户，删除失败！");
            if(user.getId().equals(userDetails.getId()))
                throw new BadRequestException("不能删除自己!");
            if(user.getAuthority().contains("root"))
                throw new InsufficientAuthenticationException("无法对超级管理员操作!");
            if(user.getAuthority().contains("admin") && !userDetails.getAuthorities().contains(new SimpleGrantedAuthority("root")))
                throw new InsufficientAuthenticationException("只有超级管理员可以删除管理员!");
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

    //更改自己账户的用户名，密码
    @PutMapping("/account")
    public JSONObject account(String new_username,String new_password) throws BadRequestException {
        if((new_username == null || new_username.isEmpty()) && (new_password == null || new_password.isEmpty()))
            throw new BadRequestException("至少需要一项不为空！");
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String myid = userDetails.getId();
        if(new_username != null && new_username.isEmpty()) {
            User user = userMapper.getUserByUsername(new_username);
            if (user != null)
                throw new BadRequestException("已经有同名用户！");
        }
        if(new_password != null) {
            if (!passwordChecker.checkPassword(new_password))
                throw new BadRequestException("密码必须由英文字母，数字或标点符号组成，且不小于6位!");
            new_password = creator.getBCryptPassword(new_password);
            new_password = "{bcrypt}" + new_password;
        }
        userMapper.updateUser(new_username,new_password,myid,null);
        json.put("message","修改成功!");

        return json;
    }

    //修改指定用户密码，用户名，最大借阅数
    @PutMapping("/editaccount")
    public JSONObject editaccount(String user_id,String new_username,String new_password,Integer max_borrow) throws BadRequestException {
        if((new_username == null || new_username.isEmpty()) && (new_password == null || new_password.isEmpty()) && max_borrow == null)
            throw new BadRequestException("至少需要一项不为空！");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        JSONObject json = new JSONObject();
        User user = userMapper.getUserById(user_id);
        if(user == null)
            throw new NoResourceFoundException("找不到用户，请检查ID！");
        if(user.getAuthority().contains("root") && !userDetails.getAuthorities().contains(new SimpleGrantedAuthority("root")))
            throw new InsufficientAuthenticationException("无法对超级管理员操作!");
        if(user.getAuthority().contains("admin") && !userDetails.getAuthorities().contains(new SimpleGrantedAuthority("root")))
            throw new InsufficientAuthenticationException("只有超级管理员可以操作管理员!");
        user = userMapper.getUserByUsername(new_username);
        if(user != null)
            throw new BadRequestException("已经有同名用户！");
        if(new_password != null) {
            if (!passwordChecker.checkPassword(new_password))
                throw new BadRequestException("密码必须由英文字母，数字或标点符号组成，且不小于6位!");
            new_password = creator.getBCryptPassword(new_password);
            new_password = "{bcrypt}" + new_password;
        }
        userMapper.updateUser(new_username,new_password,user_id,max_borrow);
        json.put("message","成功更改!");
        return json;
    }
}
