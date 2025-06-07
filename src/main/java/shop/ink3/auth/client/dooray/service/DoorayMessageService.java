package shop.ink3.auth.client.dooray.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.ink3.auth.client.dooray.client.DoorayMessageClient;
import shop.ink3.auth.client.dooray.dto.DoorayMessageRequest;

@RequiredArgsConstructor
@Service
public class DoorayMessageService {
    private final DoorayMessageClient doorayMessageClient;

    public void sendMessage(DoorayMessageRequest doorayMessageRequest) {
        doorayMessageClient.sendMessage(doorayMessageRequest);
    }
}
