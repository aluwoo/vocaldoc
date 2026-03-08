package com.vocaldoc.controller;

import com.vocaldoc.common.result.Result;
import com.vocaldoc.dto.LoginDTO;
import com.vocaldoc.dto.RegisterDTO;
import com.vocaldoc.dto.TokenDTO;
import com.vocaldoc.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<TokenDTO> login(@Validated @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String ip = getClientIp(request);
        TokenDTO tokenDTO = authService.login(loginDTO, ip);
        return Result.success(tokenDTO);
    }

    @PostMapping("/register")
    public Result<TokenDTO> register(@Validated @RequestBody RegisterDTO registerDTO, HttpServletRequest request) {
        String ip = getClientIp(request);
        TokenDTO tokenDTO = authService.register(registerDTO, ip);
        return Result.success(tokenDTO);
    }

    @PostMapping("/refresh")
    public Result<TokenDTO> refresh(@RequestBody RefreshRequest refreshRequest) {
        TokenDTO tokenDTO = authService.refreshToken(refreshRequest.getRefreshToken());
        return Result.success(tokenDTO);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public static class RefreshRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
