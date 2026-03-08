package com.vocaldoc.common.enums;

public enum TeacherStudentStatus {
    PENDING("待确认"),
    ACTIVE("已加入"),
    REMOVED("已移除");

    private final String description;

    TeacherStudentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
