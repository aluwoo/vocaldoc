package com.vocaldoc.controller;

import com.vocaldoc.common.exception.BusinessException;
import com.vocaldoc.common.result.Result;
import com.vocaldoc.common.enums.UserRole;
import com.vocaldoc.dto.InviteStudentDTO;
import com.vocaldoc.entity.User;
import com.vocaldoc.service.TeacherStudentService;
import com.vocaldoc.service.UserService;
import com.vocaldoc.vo.InviteCodeVO;
import com.vocaldoc.vo.StudentVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teachers")
@Validated
public class TeacherController {

    private final TeacherStudentService teacherStudentService;
    private final UserService userService;

    public TeacherController(TeacherStudentService teacherStudentService, UserService userService) {
        this.teacherStudentService = teacherStudentService;
        this.userService = userService;
    }

    /**
     * Get students list for a teacher
     */
    @GetMapping("/{id}/students")
    public Result<List<StudentVO>> getStudents(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        User currentUser = userService.getUserById(currentUserId);

        if (!Objects.equals(currentUserId, id) && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "无权限查看其他教师的学生列表");
        }

        List<User> students = teacherStudentService.getStudentsByTeacherId(id);
        List<StudentVO> studentVOList = students.stream()
                .map(StudentVO::fromUser)
                .collect(Collectors.toList());
        
        return Result.success(studentVOList);
    }

    /**
     * Generate invite code for a teacher
     */
    @GetMapping("/{id}/students/invite")
    public Result<InviteCodeVO> getInviteCode(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        User currentUser = userService.getUserById(currentUserId);

        if (!Objects.equals(currentUserId, id) && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "无权限生成其他教师的邀请码");
        }

        User teacher = userService.getUserById(id);
        if (teacher == null) {
            throw new BusinessException(404, "教师不存在");
        }

        String inviteCode = teacherStudentService.generateInviteCode(id);
        return Result.success(new InviteCodeVO(inviteCode, teacher.getName()));
    }

    /**
     * Invite a student by student ID
     */
    @PostMapping("/{id}/students/invite")
    public Result<InviteCodeVO> inviteStudent(@PathVariable Long id,
                                               @Valid @RequestBody InviteStudentDTO dto,
                                               HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        User currentUser = userService.getUserById(currentUserId);

        if (!Objects.equals(currentUserId, id) && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "无权限邀请其他教师的学生");
        }

        teacherStudentService.inviteStudent(id, dto.getStudentId());
        
        String inviteCode = teacherStudentService.generateInviteCode(id);
        User teacher = userService.getUserById(id);
        
        return Result.success(new InviteCodeVO(inviteCode, teacher.getName()));
    }

    /**
     * Remove a student from teacher's class
     */
    @DeleteMapping("/{id}/students/{studentId}")
    public Result<Void> removeStudent(@PathVariable Long id,
                                      @PathVariable Long studentId,
                                      HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        User currentUser = userService.getUserById(currentUserId);

        if (!Objects.equals(currentUserId, id) && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "无权限移除其他教师的学生");
        }

        teacherStudentService.removeStudent(id, studentId);
        return Result.success();
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        // This would typically be extracted from JWT
        // For now, we'll assume there's a method to get current user
        // This should be implemented properly with JWT
        String userIdStr = request.getAttribute("userId") != null 
            ? request.getAttribute("userId").toString() 
            : null;
        
        if (userIdStr == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        
        return Long.parseLong(userIdStr);
    }
}
