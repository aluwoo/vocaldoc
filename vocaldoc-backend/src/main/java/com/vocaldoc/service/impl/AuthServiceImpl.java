package com.vocaldoc.service.impl;

import com.vocaldoc.common.exception.BusinessException;
import com.vocaldoc.dto.LoginDTO;
import com.vocaldoc.dto.RegisterDTO;
import com.vocaldoc.dto.TokenDTO;
import com.vocaldoc.entity.User;
import com.vocaldoc.service.AuthService;
import com.vocaldoc.service.SmsService;
import com.vocaldoc.service.UserService;
import com.vocaldoc.util.JwtUtil;
import com.vocaldoc.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final SmsService smsService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(SmsService smsService, UserService userService, JwtUtil jwtUtil) {
        this.smsService = smsService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public TokenDTO login(LoginDTO loginDTO, String ip) {
        String phone = loginDTO.getPhone();
        String code = loginDTO.getCode();

        boolean verified = smsService.verifyCode(phone, code, "login");
        if (!verified) {
            throw new BusinessException(400, "验证码错误或已过期");
        }

        User user = userService.getByPhone(phone);
        if (user == null) {
            throw new BusinessException(404, "用户不存在，请先注册");
        }

        userService.updateLastLogin(user.getId(), ip);

        return generateTokenDTO(user);
    }

    @Override
    public TokenDTO register(RegisterDTO registerDTO, String ip) {
        String phone = registerDTO.getPhone();
        String code = registerDTO.getCode();

        boolean verified = smsService.verifyCode(phone, code, "register");
        if (!verified) {
            throw new BusinessException(400, "验证码错误或已过期");
        }

        User existUser = userService.getByPhone(phone);
        if (existUser != null) {
            throw new BusinessException(400, "手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setName(registerDTO.getName());
        user.setRole(registerDTO.getRole());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());

        if (StringUtils.hasText(registerDTO.getPassword())) {
            user.setPassword(registerDTO.getPassword());
        }

        userService.save(user);

        return generateTokenDTO(user);
    }

    @Override
    public TokenDTO refreshToken(String refreshToken) {
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(401, "无效的refresh token");
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new BusinessException(401, "refresh token已过期");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String phone = jwtUtil.getPhone(refreshToken);

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        return generateTokenDTO(user);
    }

    private TokenDTO generateTokenDTO(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken(accessToken);
        tokenDTO.setRefreshToken(refreshToken);
        tokenDTO.setExpiresIn(jwtUtil.getAccessTokenExpiration());
        return tokenDTO;
    }
}
