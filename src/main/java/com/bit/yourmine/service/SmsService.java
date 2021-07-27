package com.bit.yourmine.service;

import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class SmsService {

    @Value("${sms.api.key}")
    private String key;
    @Value("${sms.api.secret}")
    private String secret;
    @Value("${sms.submit.num}")
    private String num;

    public void sendSms(String phone) {
        Message sms = new Message(key, secret);
        HashMap<String, String> params = new HashMap<String, String>();
        String cutPhone = phone.replace("-", "");
        params.put("to", cutPhone);
        params.put("from", num);
        params.put("type", "SMS");
        params.put("text", "YourMine에서 새로운 대화가 시작되었습니다!");
        params.put("app_version", "test app 1.2");
        try {
            JSONObject obj = (JSONObject) sms.send(params);
            System.out.println(obj.toString());
        } catch (CoolsmsException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
        }
    }
}