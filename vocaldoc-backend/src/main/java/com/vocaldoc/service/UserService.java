package com.vocaldoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vocaldoc.entity.User;

public interface UserService extends IService<User> {

    User getByPhone(String phone);

    void updateLastLogin(Long userId, String ip);

    User getUserById(Long id);

    void updateUser(User user);

    void deleteUser(Long id);
}
