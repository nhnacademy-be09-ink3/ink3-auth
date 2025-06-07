package shop.ink3.auth.client.dooray.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shop.ink3.auth.client.dooray.dto.DoorayMessageRequest;

@FeignClient(name = "messageSendClient", url = "${dooray.message-url}")
public interface DoorayMessageClient {
    @PostMapping
    String sendMessage(@RequestBody DoorayMessageRequest doorayMessageRequest);
}
