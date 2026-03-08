package com.vocaldoc.vo;

public class InviteCodeVO {

    private String inviteCode;
    private String teacherName;

    public InviteCodeVO(String inviteCode, String teacherName) {
        this.inviteCode = inviteCode;
        this.teacherName = teacherName;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
