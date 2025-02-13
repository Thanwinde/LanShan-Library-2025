package com.LanShan.Library.securityconfig;

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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
//自定义UserDetail和UserDetailsManager
//实现自定义认证和用户信息定制
@Component
public class DBUserDetailsManager implements UserDetailsManager  {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.getUserByUsername(username);

        if (user == null) {
            user = userMapper.getUserByEmail(username);
            if (user == null)
                throw new UsernameNotFoundException(username);
        }
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String[] row_authorities = user.getAuthority().split(",");
        for (String row_authority : row_authorities) {
            authorities.add(new SimpleGrantedAuthority(row_authority));
        }
        return new MyUserDetails(
                user.getUsername(),
                user.getPassword(),
                user.getId(),
                user.getEmail(),
                authorities); //用户凭证信息列表

    }
    //用不到的功能

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
