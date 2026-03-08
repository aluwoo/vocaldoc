package com.vocaldoc.service;

public interface SmsService {

    void sendCode(String phone, String type);

    boolean verifyCode(String phone, String code, String type);
}
