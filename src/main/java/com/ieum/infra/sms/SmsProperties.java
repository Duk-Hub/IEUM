package com.ieum.infra.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.coolsms")
public record SmsProperties(
        String apiKey,
        String apiSecret,
        String from
) {
}
