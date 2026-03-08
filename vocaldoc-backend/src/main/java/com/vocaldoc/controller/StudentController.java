package com.vocaldoc.controller;

import com.vocaldoc.common.exception.BusinessException;
import com.vocaldoc.common.result.Result;
import com.vocaldoc.dto.JoinClassDTO;
import com.vocaldoc.entity.User;
import com.vocaldoc.service.TeacherStudentService;
import com.vocaldoc.service.UserService;
import com.vocaldoc.vo.InviteCodeVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/students")
@Validated
public class StudentController {

    private final TeacherStudentService teacherStudentService;
    private final UserService userService;

    public StudentController(TeacherStudentService teacherStudentService, UserService userService) {
        this.teacherStudentService = teacherStudentService;
        this.userService = userService;
    }

    @PostMapping("/join")
    public Result<InviteCodeVO> joinClass(@Valid @RequestBody JoinClassDTO dto, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        User currentUser = userService.getUserById(currentUserId);
        
        if (currentUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        teacherStudentService.joinByInviteCode(currentUserId, dto.getInviteCode());
        
        return Result.success();
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getAttribute("userId") != null 
            ? request.getAttribute("userId").toString() 
            : null;
        
        if (userIdStr == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        
        return Long.parseLong(userIdStr);
    }
}
