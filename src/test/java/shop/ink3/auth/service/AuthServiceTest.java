package shop.ink3.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import shop.ink3.auth.client.UserClient;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.dto.JwtToken;
import shop.ink3.auth.dto.LoginRequest;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.UserType;
import shop.ink3.auth.exception.InvalidPasswordException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserClient userClient;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    TokenService tokenService;

    @InjectMocks
    AuthService authService;

    @Test
    void loginUser() {
        LoginRequest request = new LoginRequest("test", "test", UserType.USER);
        AuthResponse user = new AuthResponse(1L, "test", "encodedPassword");
        JwtToken accessToken = new JwtToken("accessToken", 1L);
        JwtToken refreshToken = new JwtToken("refreshToken", 2L);

        when(userClient.getUser("test")).thenReturn(CommonResponse.success(user));
        when(passwordEncoder.matches("test", "encodedPassword")).thenReturn(true);
        when(tokenService.issueTokens(user.id(), user.username(), UserType.USER))
                .thenReturn(new LoginResponse(accessToken, refreshToken));

        LoginResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        verify(userClient).updateUserLastLogin(1L);
    }

    @Test
    void loginAdmin() {
        LoginRequest request = new LoginRequest("test", "test", UserType.ADMIN);
        AuthResponse admin = new AuthResponse(1L, "test", "encodedPassword");
        JwtToken accessToken = new JwtToken("accessToken", 1L);
        JwtToken refreshToken = new JwtToken("refreshToken", 2L);

        when(userClient.getAdmin("test")).thenReturn(CommonResponse.success(admin));
        when(passwordEncoder.matches("test", "encodedPassword")).thenReturn(true);
        when(tokenService.issueTokens(admin.id(), admin.username(), UserType.ADMIN))
                .thenReturn(new LoginResponse(accessToken, refreshToken));

        LoginResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        verify(userClient).updateAdminLastLogin(1L);
    }

    @Test
    void loginWithInvalidPassword() {
        LoginRequest request = new LoginRequest("test", "invalidPassword", UserType.USER);
        AuthResponse user = new AuthResponse(1L, "test", "encodedPassword");

        when(userClient.getUser("test")).thenReturn(CommonResponse.success(user));
        when(passwordEncoder.matches("invalidPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void logout() {
        authService.logout("accessToken");
        verify(tokenService).invalidateTokens("accessToken");
    }
}
