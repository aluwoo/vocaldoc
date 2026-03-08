package com.vocaldoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vocaldoc.common.enums.UserRole;
import com.vocaldoc.entity.TeacherStudent;
import com.vocaldoc.entity.User;
import com.vocaldoc.mapper.TeacherStudentMapper;
import com.vocaldoc.mapper.UserMapper;
import com.vocaldoc.service.AdminService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final TeacherStudentMapper teacherStudentMapper;

    public AdminServiceImpl(UserMapper userMapper, TeacherStudentMapper teacherStudentMapper) {
        this.userMapper = userMapper;
        this.teacherStudentMapper = teacherStudentMapper;
    }

    @Override
    public Page<User> getTeacherList(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, UserRole.TEACHER);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getName, keyword)
                   .or()
                   .like(User::getPhone, keyword);
        }
        
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public void createTeacher(User user) {
        user.setRole(UserRole.TEACHER);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public void updateTeacher(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void deleteTeacher(Long id) {
        User teacher = userMapper.selectById(id);
        if (teacher != null && teacher.getRole() == UserRole.TEACHER) {
            userMapper.deleteById(id);
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<User> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(User::getRole, UserRole.TEACHER);
        long teacherCount = userMapper.selectCount(teacherWrapper);
        
        LambdaQueryWrapper<User> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(User::getRole, UserRole.STUDENT);
        long studentCount = userMapper.selectCount(studentWrapper);
        
        stats.put("teacherCount", teacherCount);
        stats.put("studentCount", studentCount);
        stats.put("totalUsers", teacherCount + studentCount);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getStudentListByTeacher(Long teacherId) {
        LambdaQueryWrapper<TeacherStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherStudent::getTeacherId, teacherId);
        List<TeacherStudent> relations = teacherStudentMapper.selectList(wrapper);
        
        List<Long> studentIds = relations.stream()
            .map(TeacherStudent::getStudentId)
            .collect(Collectors.toList());
        
        if (studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<User> students = userMapper.selectBatchIds(studentIds);
        return students.stream().map(this::userToMap).collect(Collectors.toList());
    }

    @Override
    public void importStudents(Long teacherId, List<User> students) {
        for (User student : students) {
            student.setRole(UserRole.STUDENT);
            student.setCreatedAt(LocalDateTime.now());
            
            if (student.getId() == null) {
                userMapper.insert(student);
            }
            
            TeacherStudent relation = new TeacherStudent();
            relation.setTeacherId(teacherId);
            relation.setStudentId(student.getId());
            relation.setStatus("1");
            relation.setCreatedAt(LocalDateTime.now());
            
            LambdaQueryWrapper<TeacherStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TeacherStudent::getTeacherId, teacherId)
                   .eq(TeacherStudent::getStudentId, student.getId());
            TeacherStudent existing = teacherStudentMapper.selectOne(wrapper);
            
            if (existing == null) {
                teacherStudentMapper.insert(relation);
            }
        }
    }

    @Override
    public void exportStudents(Long teacherId) {
    }

    @Override
    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("smsEnabled", true);
        config.put("ossEnabled", true);
        config.put("maxUploadSize", 50 * 1024 * 1024);
        config.put("allowedAudioFormats", Arrays.asList("wav", "flac", "mp3", "m4a"));
        return config;
    }

    @Override
    public void updateSystemConfig(Map<String, Object> config) {
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole());
        return map;
    }
}
