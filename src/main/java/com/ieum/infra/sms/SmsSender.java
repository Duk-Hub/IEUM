package com.ieum.infra.sms;

public interface SmsSender {
    void send(String to, String content);
}
