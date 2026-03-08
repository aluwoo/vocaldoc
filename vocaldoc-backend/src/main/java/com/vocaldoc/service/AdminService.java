package com.vocaldoc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vocaldoc.entity.User;
import java.util.List;
import java.util.Map;

public interface AdminService {

    Page<User> getTeacherList(int pageNum, int pageSize, String keyword);

    void createTeacher(User user);

    void updateTeacher(User user);

    void deleteTeacher(Long id);

    Map<String, Object> getStatistics();

    List<Map<String, Object>> getStudentListByTeacher(Long teacherId);

    void importStudents(Long teacherId, List<User> students);

    void exportStudents(Long teacherId);

    Map<String, Object> getSystemConfig();

    void updateSystemConfig(Map<String, Object> config);
}
