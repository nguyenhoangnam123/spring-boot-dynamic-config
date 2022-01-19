package com.ptfmobile.vn.authservice.client;

import com.ptfmobile.vn.common.message.SendEmailPayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "notify-service", url = "${client.notify.baseUrl}")
public interface NotifyFeignClient {
  @PostMapping("/mails")
  String sendEmail(SendEmailPayload payload);
}