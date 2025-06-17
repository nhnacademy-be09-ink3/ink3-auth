package shop.ink3.auth.client.dooray.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.ink3.auth.client.dooray.client.DoorayMessageClient;
import shop.ink3.auth.client.dooray.dto.DoorayMessageRequest;

@ExtendWith(MockitoExtension.class)
class DoorayMessageServiceTest {
    @Mock
    DoorayMessageClient doorayMessageClient;

    @InjectMocks
    DoorayMessageService doorayMessageService;

    @Test
    void sendMessage() {
        DoorayMessageRequest request = new DoorayMessageRequest("bot", "test.", List.of());

        doorayMessageService.sendMessage(request);

        verify(doorayMessageClient, times(1)).sendMessage(request);
    }
}
