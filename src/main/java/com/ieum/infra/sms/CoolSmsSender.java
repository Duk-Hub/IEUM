package com.ieum.infra.sms;

import com.ieum.global.exception.CustomException;
import com.ieum.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Component;

@Slf4j
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

        try {
            messageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception e) {
            log.error("SMS 발송 실패. to={}", to, e);
            throw new CustomException(ErrorCode.SMS_SEND_FAILED);
        }
    }
}
