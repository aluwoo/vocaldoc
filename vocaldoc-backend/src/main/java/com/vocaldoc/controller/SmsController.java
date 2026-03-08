package com.vocaldoc.controller;

import com.vocaldoc.common.result.Result;
import com.vocaldoc.dto.SmsSendDTO;
import com.vocaldoc.dto.SmsVerifyDTO;
import com.vocaldoc.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @PostMapping("/send")
    public Result<Void> sendCode(@Validated @RequestBody SmsSendDTO dto) {
        smsService.sendCode(dto.getPhone(), dto.getType());
        return Result.success();
    }

    @PostMapping("/verify")
    public Result<Boolean> verifyCode(@Validated @RequestBody SmsVerifyDTO dto) {
        boolean valid = smsService.verifyCode(dto.getPhone(), dto.getCode(), dto.getType());
        return Result.success(valid);
    }
}
