package com.vocaldoc.dto;

import javax.validation.constraints.NotBlank;

public class JoinClassDTO {

    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
