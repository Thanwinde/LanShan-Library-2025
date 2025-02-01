package com.LanShan.Library.service;

import com.LanShan.Library.mapper.UserMapper;
import com.LanShan.Library.pojo.MyUserDetails;
import jakarta.annotation.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import com.LanShan.Library.pojo.User;

import java.util.ArrayList;
import java.util.Collection;
//自定义UserDetail和UserDetailsManager
//实现自定义认证和用户信息定制
public class DBUserDetailsManager implements UserDetailsManager, UserDetailsPasswordService {
    
    @Resource
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        /*QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);*/
        User user = userMapper.GetUserByUsername(username);

        if (user == null) {
            //System.out.println("用户不存在");
            throw new UsernameNotFoundException(username);
        } else {
            //System.out.println("用户存在!!!!!!");
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(user.getAuthority()));
            //System.out.println(user.getPassword());
            return new MyUserDetails(
                    user.getUsername(),
                    user.getPassword(),
                    user.getId(),
                    authorities); //用户凭证信息列表
        }
    }
    //用不到的功能
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return user;//疑似是BUG，明明没加这功能每次都会跳到这个API
    }

    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }
}
