package com.vocaldoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vocaldoc.entity.User;
import com.vocaldoc.mapper.UserMapper;
import com.vocaldoc.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return this.getOne(wrapper);
    }

    @Override
    public void updateLastLogin(Long userId, String ip) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        this.updateById(user);
    }

    @Override
    public User getUserById(Long id) {
        return this.getById(id);
    }

    @Override
    public void updateUser(User user) {
        this.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setDeletedAt(LocalDateTime.now());
        this.updateById(user);
    }
}
