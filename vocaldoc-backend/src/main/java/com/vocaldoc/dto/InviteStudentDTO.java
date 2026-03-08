package com.vocaldoc.dto;

import javax.validation.constraints.NotNull;

public class InviteStudentDTO {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}
