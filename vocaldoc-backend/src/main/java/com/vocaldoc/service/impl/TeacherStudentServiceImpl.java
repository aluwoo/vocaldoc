package com.vocaldoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vocaldoc.common.enums.TeacherStudentStatus;
import com.vocaldoc.common.exception.BusinessException;
import com.vocaldoc.entity.TeacherStudent;
import com.vocaldoc.entity.User;
import com.vocaldoc.mapper.TeacherStudentMapper;
import com.vocaldoc.mapper.UserMapper;
import com.vocaldoc.service.TeacherStudentService;
import com.vocaldoc.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeacherStudentServiceImpl extends ServiceImpl<TeacherStudentMapper, TeacherStudent> implements TeacherStudentService {

    private final UserService userService;

    public TeacherStudentServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String generateInviteCode(Long teacherId) {
        LambdaQueryWrapper<TeacherStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherStudent::getTeacherId, teacherId)
                .eq(TeacherStudent::getStatus, TeacherStudentStatus.ACTIVE.getDescription());
        
        TeacherStudent existing = this.getOne(wrapper);
        if (existing != null && existing.getInviteCode() != null) {
            return existing.getInviteCode();
        }

        return "V" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional
    public TeacherStudent inviteStudent(Long teacherId, Long studentId) {
        User teacher = userService.getUserById(teacherId);
        if (teacher == null) {
            throw new BusinessException(404, "教师不存在");
        }

        User student = userService.getUserById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        TeacherStudent existing = getByTeacherAndStudent(teacherId, studentId);
        if (existing != null) {
            if (existing.getStatus() == TeacherStudentStatus.ACTIVE.getDescription()) {
                throw new BusinessException(400, "该学生已在班级中");
            }
            String inviteCode = generateInviteCode(teacherId);
            existing.setInviteCode(inviteCode);
            existing.setStatus(TeacherStudentStatus.PENDING.getDescription());
            existing.setUpdatedAt(LocalDateTime.now());
            this.updateById(existing);
            return existing;
        }

        TeacherStudent relation = new TeacherStudent();
        relation.setTeacherId(teacherId);
        relation.setStudentId(studentId);
        relation.setInviteCode(generateInviteCode(teacherId));
        relation.setStatus(TeacherStudentStatus.PENDING.getDescription());
        relation.setCreatedAt(LocalDateTime.now());
        relation.setUpdatedAt(LocalDateTime.now());
        this.save(relation);
        return relation;
    }

    @Override
    public List<User> getStudentsByTeacherId(Long teacherId) {
        LambdaQueryWrapper<TeacherStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherStudent::getTeacherId, teacherId)
                .eq(TeacherStudent::getStatus, TeacherStudentStatus.ACTIVE.getDescription());
        
        List<TeacherStudent> relations = this.list(wrapper);
        
        List<Long> studentIds = relations.stream()
                .map(TeacherStudent::getStudentId)
                .collect(Collectors.toList());
        
        if (studentIds.isEmpty()) {
            return List.of();
        }
        
        return userService.listByIds(studentIds);
    }

    @Override
    @Transactional
    public void removeStudent(Long teacherId, Long studentId) {
        TeacherStudent relation = getByTeacherAndStudent(teacherId, studentId);
        if (relation == null) {
            throw new BusinessException(404, "该学生不在班级中");
        }
        
        relation.setStatus(TeacherStudentStatus.REMOVED.getDescription());
        relation.setUpdatedAt(LocalDateTime.now());
        this.updateById(relation);
    }

    @Override
    public TeacherStudent getByInviteCode(String inviteCode) {
        LambdaQueryWrapper<TeacherStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherStudent::getInviteCode, inviteCode)
                .eq(TeacherStudent::getStatus, TeacherStudentStatus.PENDING.getDescription());
        return this.getOne(wrapper);
    }

    @Override
    @Transactional
    public TeacherStudent joinByInviteCode(Long studentId, String inviteCode) {
        TeacherStudent relation = getByInviteCode(inviteCode);
        if (relation == null) {
            throw new BusinessException(400, "邀请码无效或已过期");
        }

        if (relation.getStudentId() != null && !relation.getStudentId().equals(studentId)) {
            throw new BusinessException(403, "邀请码不属于该学生");
        }

        relation.setStudentId(studentId);
        relation.setStatus(TeacherStudentStatus.ACTIVE.getDescription());
        relation.setJoinedAt(LocalDateTime.now());
        relation.setUpdatedAt(LocalDateTime.now());
        this.updateById(relation);
        
        return relation;
    }

    @Override
    public TeacherStudent getByTeacherAndStudent(Long teacherId, Long studentId) {
        LambdaQueryWrapper<TeacherStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherStudent::getTeacherId, teacherId)
                .eq(TeacherStudent::getStudentId, studentId);
        return this.getOne(wrapper);
    }
}
