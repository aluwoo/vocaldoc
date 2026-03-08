package com.vocaldoc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vocaldoc.common.result.Result;
import com.vocaldoc.entity.User;
import com.vocaldoc.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/teachers")
    public Result<Page<User>> getTeachers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<User> page = adminService.getTeacherList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping("/teachers")
    public Result<Void> createTeacher(@RequestBody User user) {
        adminService.createTeacher(user);
        return Result.success(null);
    }

    @PutMapping("/teachers/{id}")
    public Result<Void> updateTeacher(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        adminService.updateTeacher(user);
        return Result.success(null);
    }

    @DeleteMapping("/teachers/{id}")
    public Result<Void> deleteTeacher(@PathVariable Long id) {
        adminService.deleteTeacher(id);
        return Result.success(null);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = adminService.getStatistics();
        return Result.success(stats);
    }

    @GetMapping("/students/{teacherId}")
    public Result<List<Map<String, Object>>> getStudents(@PathVariable Long teacherId) {
        List<Map<String, Object>> students = adminService.getStudentListByTeacher(teacherId);
        return Result.success(students);
    }

    @PostMapping("/students/import/{teacherId}")
    public Result<Void> importStudents(@PathVariable Long teacherId, @RequestBody List<User> students) {
        adminService.importStudents(teacherId, students);
        return Result.success(null);
    }

    @GetMapping("/students/export/{teacherId}")
    public Result<Void> exportStudents(@PathVariable Long teacherId) {
        adminService.exportStudents(teacherId);
        return Result.success(null);
    }

    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = adminService.getSystemConfig();
        return Result.success(config);
    }

    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody Map<String, Object> config) {
        adminService.updateSystemConfig(config);
        return Result.success(null);
    }
}
