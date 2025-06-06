package shop.ink3.auth.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.ink3.auth.client.dooray.dto.Attachment;
import shop.ink3.auth.client.dooray.dto.DoorayMessageRequest;
import shop.ink3.auth.client.dooray.service.DoorayMessageService;
import shop.ink3.auth.client.user.UserClient;
import shop.ink3.auth.client.user.dto.UserStatus;
import shop.ink3.auth.client.user.dto.UserStatusResponse;
import shop.ink3.auth.dto.SendReactiveCodeRequest;
import shop.ink3.auth.dto.VerifyReactiveCodeRequest;
import shop.ink3.auth.exception.InvalidReactiveCodeException;
import shop.ink3.auth.exception.InvalidUserStateException;
import shop.ink3.auth.repository.ReactiveCodeRepository;

@RequiredArgsConstructor
@Service
public class ReactiveService {
    private final ReactiveCodeRepository reactiveCodeRepository;
    private final DoorayMessageService doorayMessageService;
    private final UserClient userClient;

    public void sendReactiveCode(SendReactiveCodeRequest request) {
        UserStatusResponse response = userClient.getUserStatus(request.loginId()).data();
        if (response.status() != UserStatus.DORMANT) {
            throw new InvalidUserStateException("User is not in dormant status.");
        }
        String code = generateAuthCode();
        reactiveCodeRepository.saveReactiveCode(request.loginId(), code);
        doorayMessageService.sendMessage(new DoorayMessageRequest(
                "휴면 해제 봇",
                "휴면 해제 코드를 입력해주세요.",
                List.of(new Attachment(
                        "휴면 해제 코드",
                        code,
                        "https://ink3.shop",
                        "https://imgur.com/a/4Li3ZTC",
                        "green"
                ))
        ));
    }

    public void reactivateUser(VerifyReactiveCodeRequest request) {
        String savedReactiveCode = reactiveCodeRepository.getReactiveCode(request.loginId());
        if (Objects.isNull(savedReactiveCode) || !savedReactiveCode.equals(request.code())) {
            throw new InvalidReactiveCodeException();
        }
        userClient.activateUser(request.loginId());
        reactiveCodeRepository.deleteReactiveCode(request.loginId());
    }

    private String generateAuthCode() {
        Random random = new SecureRandom();
        int code = random.nextInt(900_000) + 100_000;
        return String.valueOf(code);
    }
}
