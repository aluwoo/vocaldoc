package com.vocaldoc.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vocaldoc.common.exception.BusinessException;
import com.vocaldoc.entity.SmsCode;
import com.vocaldoc.mapper.SmsCodeMapper;
import com.vocaldoc.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int RESEND_INTERVAL_SECONDS = 60;

    @Autowired
    private SmsCodeMapper smsCodeMapper;

    @Autowired
    private Client smsClient;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String templateCode;

    @Override
    @Transactional
    public void sendCode(String phone, String type) {
        validatePhoneAndType(phone, type);

        LambdaQueryWrapper<SmsCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmsCode::getPhone, phone)
                .eq(SmsCode::getType, type)
                .orderByDesc(SmsCode::getCreatedAt)
                .last("LIMIT 1");
        
        SmsCode lastCode = smsCodeMapper.selectOne(queryWrapper);
        
        if (lastCode != null) {
            LocalDateTime now = LocalDateTime.now();
            if (lastCode.getCreatedAt() != null) {
                long secondsSinceLastCode = java.time.Duration.between(lastCode.getCreatedAt(), now).getSeconds();
                if (secondsSinceLastCode < RESEND_INTERVAL_SECONDS) {
                    throw new BusinessException(400, "发送过于频繁，请" + (RESEND_INTERVAL_SECONDS - secondsSinceLastCode) + "秒后重试");
                }
            }
            
            if (lastCode.getExpiredAt() != null && lastCode.getExpiredAt().isAfter(now)) {
                throw new BusinessException(400, "验证码未过期，请使用现有验证码");
            }
        }

        String code = generateCode();
        
        try {
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");
            
            SendSmsResponse response = smsClient.sendSms(sendSmsRequest);
            
            if (response == null || !"OK".equals(response.getBody().getCode())) {
                String message = response != null ? response.getBody().getMessage() : "发送失败";
                log.error("短信发送失败: phone={}, type={}, response={}", phone, type, response);
                throw new BusinessException(500, "短信发送失败: " + message);
            }
            
            log.info("短信发送成功: phone={}, type={}", phone, type);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("短信发送异常: phone={}, type={}, error={}", phone, type, e.getMessage(), e);
            throw new BusinessException(500, "短信发送异常: " + e.getMessage());
        }

        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(phone);
        smsCode.setCode(code);
        smsCode.setType(type);
        smsCode.setExpiredAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        smsCode.setStatus(0);
        smsCode.setCreatedAt(LocalDateTime.now());
        
        smsCodeMapper.insert(smsCode);
    }

    @Override
    public boolean verifyCode(String phone, String code, String type) {
        validatePhoneAndType(phone, type);
        
        if (code == null || code.length() != CODE_LENGTH) {
            return false;
        }

        LambdaQueryWrapper<SmsCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmsCode::getPhone, phone)
                .eq(SmsCode::getCode, code)
                .eq(SmsCode::getType, type)
                .gt(SmsCode::getExpiredAt, LocalDateTime.now())
                .eq(SmsCode::getStatus, 0)
                .orderByDesc(SmsCode::getCreatedAt)
                .last("LIMIT 1");
        
        SmsCode smsCode = smsCodeMapper.selectOne(queryWrapper);
        
        if (smsCode == null) {
            log.warn("验证码验证失败: phone={}, type={}, code={}", phone, type, code);
            return false;
        }

        smsCode.setStatus(1);
        smsCode.setUsedAt(LocalDateTime.now());
        smsCodeMapper.updateById(smsCode);
        
        log.info("验证码验证成功: phone={}, type={}", phone, type);
        return true;
    }

    private void validatePhoneAndType(String phone, String type) {
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        
        if (type == null || type.isEmpty()) {
            throw new BusinessException(400, "验证码类型不能为空");
        }
        
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        
        if (!isValidType(type)) {
            throw new BusinessException(400, "无效的验证码类型");
        }
    }

    private boolean isValidType(String type) {
        return "LOGIN".equals(type) || "REGISTER".equals(type) || "RESET_PWD".equals(type);
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
