package com.vocaldoc.controller;

import com.vocaldoc.common.exception.BusinessException;
import com.vocaldoc.common.result.Result;
import com.vocaldoc.common.enums.UserRole;
import com.vocaldoc.dto.UserUpdateDTO;
import com.vocaldoc.entity.User;
import com.vocaldoc.service.UserService;
import com.vocaldoc.util.JwtUtil;
import com.vocaldoc.vo.UserVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        User user = userService.getUserById(currentUserId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return Result.success(convertToVO(user));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        User currentUser = userService.getUserById(currentUserId);

        if (!Objects.equals(currentUserId, id) && !isAdminOrTeacher(currentUser)) {
            throw new BusinessException(403, "无权限查看其他用户信息");
        }

        User user = userService.getUserById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return Result.success(convertToVO(user));
    }

    /**
     * Update user info
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id,
                                    @Valid @RequestBody UserUpdateDTO updateDTO,
                                    HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        User currentUser = userService.getUserById(currentUserId);

        if (!Objects.equals(currentUserId, id) && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "无权限修改其他用户信息");
        }

        if (updateDTO.getRole() != null && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "无权限修改用户角色");
        }

        if (updateDTO.getStatus() != null && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "无权限修改用户状态");
        }

        User user = new User();
        user.setId(id);
        if (updateDTO.getName() != null) {
            user.setName(updateDTO.getName());
        }
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }
        if (updateDTO.getRole() != null && currentUser.getRole() == UserRole.ADMIN) {
            user.setRole(updateDTO.getRole());
        }
        if (updateDTO.getStatus() != null && currentUser.getRole() == UserRole.ADMIN) {
            user.setStatus(updateDTO.getStatus());
        }

        userService.updateUser(user);
        return Result.success();
    }

    /**
     * Delete (soft delete) user
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        User currentUser = userService.getUserById(currentUserId);

        if (!Objects.equals(currentUserId, id) && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "无权限删除其他用户");
        }

        User targetUser = userService.getUserById(id);
        if (targetUser.getRole() == UserRole.ADMIN) {
            throw new BusinessException(403, "无法删除管理员账户");
        }

        userService.deleteUser(id);
        return Result.success();
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        return jwtUtil.getUserId(token);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTH_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean isAdminOrTeacher(User user) {
        return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.TEACHER;
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setName(user.getName());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setLastLoginAt(user.getLastLoginAt());
        return vo;
    }
}
