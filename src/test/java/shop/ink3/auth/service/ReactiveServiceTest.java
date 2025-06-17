package shop.ink3.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.ink3.auth.client.dooray.dto.DoorayMessageRequest;
import shop.ink3.auth.client.dooray.service.DoorayMessageService;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.client.user.UserClient;
import shop.ink3.auth.client.user.dto.UserStatus;
import shop.ink3.auth.client.user.dto.UserStatusResponse;
import shop.ink3.auth.dto.SendReactiveCodeRequest;
import shop.ink3.auth.dto.VerifyReactiveCodeRequest;
import shop.ink3.auth.exception.InvalidReactiveCodeException;
import shop.ink3.auth.exception.InvalidUserStateException;
import shop.ink3.auth.repository.ReactiveCodeRepository;

@ExtendWith(MockitoExtension.class)
class ReactiveServiceTest {
    @Mock
    ReactiveCodeRepository reactiveCodeRepository;
    @Mock
    DoorayMessageService doorayMessageService;
    @Mock
    UserClient userClient;

    @InjectMocks
    ReactiveService reactiveService;

    @Test
    void sendReactiveCode() {
        SendReactiveCodeRequest request = new SendReactiveCodeRequest("user");
        when(userClient.getUserStatus("user"))
                .thenReturn(CommonResponse.success(new UserStatusResponse(UserStatus.DORMANT)));

        reactiveService.sendReactiveCode(request);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(reactiveCodeRepository).saveReactiveCode(eq("user"), codeCaptor.capture());
        String code = codeCaptor.getValue();
        assertThat(code).matches("\\d{6}");

        ArgumentCaptor<DoorayMessageRequest> requestCaptor = ArgumentCaptor.forClass(DoorayMessageRequest.class);
        verify(doorayMessageService).sendMessage(requestCaptor.capture());
        assertThat(requestCaptor.getValue().attachments().getFirst().text()).isEqualTo(code);
    }

    @Test
    void sendReactiveCodeWithInvalidState() {
        SendReactiveCodeRequest request = new SendReactiveCodeRequest("user");
        when(userClient.getUserStatus("user"))
                .thenReturn(CommonResponse.success(new UserStatusResponse(UserStatus.ACTIVE)));

        assertThatThrownBy(() -> reactiveService.sendReactiveCode(request))
                .isInstanceOf(InvalidUserStateException.class);
    }

    @Test
    void reactivateUser() {
        VerifyReactiveCodeRequest request = new VerifyReactiveCodeRequest("user", "123456");
        when(reactiveCodeRepository.getReactiveCode("user")).thenReturn("123456");

        reactiveService.reactivateUser(request);

        verify(userClient).activateUser("user");
        verify(reactiveCodeRepository).deleteReactiveCode("user");
    }

    @Test
    void reactivateUserWithInvalidCode() {
        VerifyReactiveCodeRequest request = new VerifyReactiveCodeRequest("user", "000000");
        when(reactiveCodeRepository.getReactiveCode("user")).thenReturn("123456");

        assertThatThrownBy(() -> reactiveService.reactivateUser(request))
                .isInstanceOf(InvalidReactiveCodeException.class);
    }

    @Test
    void reactivateUserWithCodeNotFound() {
        VerifyReactiveCodeRequest request = new VerifyReactiveCodeRequest("user", "000000");
        when(reactiveCodeRepository.getReactiveCode("user")).thenReturn(null);

        assertThatThrownBy(() -> reactiveService.reactivateUser(request))
                .isInstanceOf(InvalidReactiveCodeException.class);
    }
}
