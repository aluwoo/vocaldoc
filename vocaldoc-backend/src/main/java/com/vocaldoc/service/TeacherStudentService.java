package com.vocaldoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vocaldoc.entity.TeacherStudent;
import com.vocaldoc.entity.User;

import java.util.List;

public interface TeacherStudentService extends IService<TeacherStudent> {

    String generateInviteCode(Long teacherId);

    TeacherStudent inviteStudent(Long teacherId, Long studentId);

    List<User> getStudentsByTeacherId(Long teacherId);

    void removeStudent(Long teacherId, Long studentId);

    TeacherStudent getByInviteCode(String inviteCode);

    TeacherStudent joinByInviteCode(Long studentId, String inviteCode);

    TeacherStudent getByTeacherAndStudent(Long teacherId, Long studentId);
}
