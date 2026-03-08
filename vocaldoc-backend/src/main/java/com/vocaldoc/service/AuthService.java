package com.vocaldoc.service;

import com.vocaldoc.dto.LoginDTO;
import com.vocaldoc.dto.RegisterDTO;
import com.vocaldoc.dto.TokenDTO;
import com.vocaldoc.vo.UserVO;

public interface AuthService {

    TokenDTO login(LoginDTO loginDTO, String ip);

    TokenDTO register(RegisterDTO registerDTO, String ip);

    TokenDTO refreshToken(String refreshToken);
}
