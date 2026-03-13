package com.ieum.infra.sms;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Component;

@Component
public class CoolSmsSender implements SmsSender {

    private final DefaultMessageService messageService;
    private final SmsProperties smsProperties;

    public CoolSmsSender(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
        this.messageService = NurigoApp.INSTANCE.initialize(
                smsProperties.apiKey(),
                smsProperties.apiSecret(),
                "https://api.coolsms.co.kr"
        );
    }

    @Override
    public void send(String to, String content) {
        Message message = new Message();
        message.setFrom(smsProperties.from());
        message.setTo(to);
        message.setText(content);

        messageService.sendOne(new SingleMessageSendingRequest(message));
    }
}
