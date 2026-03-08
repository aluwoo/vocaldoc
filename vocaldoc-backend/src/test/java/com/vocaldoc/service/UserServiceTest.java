package com.vocaldoc.service;

import com.vocaldoc.entity.User;
import com.vocaldoc.common.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private com.vocaldoc.mapper.UserMapper userMapper;

    @InjectMocks
    private com.vocaldoc.service.impl.UserServiceImpl userService;

    @Test
    public void testGetByPhone() {
        User user = new User();
        user.setId(1L);
        user.setPhone("13800138000");
        user.setName("Test User");
        user.setRole(UserRole.STUDENT);

        when(userMapper.selectOne(any())).thenReturn(user);

        User result = userService.getByPhone("13800138000");

        assertNotNull(result);
        assertEquals("13800138000", result.getPhone());
        verify(userMapper, times(1)).selectOne(any());
    }

    @Test
    public void testGetByPhoneNotFound() {
        when(userMapper.selectOne(any())).thenReturn(null);

        User result = userService.getByPhone("13900139000");

        assertNull(result);
    }

    @Test
    public void testUpdateLastLogin() {
        User user = new User();
        user.setId(1L);

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);

        userService.updateLastLogin(1L, "127.0.0.1");

        verify(userMapper, times(1)).updateById(any());
    }
}
