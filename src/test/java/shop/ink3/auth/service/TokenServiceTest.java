package shop.ink3.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.client.user.UserClient;
import shop.ink3.auth.client.user.dto.AuthResponse;
import shop.ink3.auth.dto.JwtToken;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.ReissueRequest;
import shop.ink3.auth.dto.UserType;
import shop.ink3.auth.exception.InvalidRefreshTokenException;
import shop.ink3.auth.jwt.JwtTokenProvider;
import shop.ink3.auth.repository.TokenRepository;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    TokenRepository tokenRepository;

    @Mock
    UserClient userClient;

    @InjectMocks
    TokenService tokenService;

    @Test
    void issueTokens() {
        AuthResponse user = new AuthResponse(1L, "username", "encodedPassword");
        JwtToken accessToken = new JwtToken("accessToken", 1L);
        JwtToken refreshToken = new JwtToken("refreshToken", 2L);

        when(jwtTokenProvider.generateAccessToken(1L, "username", UserType.USER)).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(1L, "username", UserType.USER)).thenReturn(refreshToken);

        LoginResponse response = tokenService.issueTokens(user.id(), user.username(), UserType.USER);

        assertThat(response.accessToken().token()).isEqualTo("accessToken");
        assertThat(response.accessToken().expiresAt()).isEqualTo(accessToken.expiresAt());
        assertThat(response.refreshToken().token()).isEqualTo("refreshToken");
        assertThat(response.refreshToken().expiresAt()).isEqualTo(refreshToken.expiresAt());

        verify(tokenRepository).saveRefreshToken(1L, UserType.USER, "refreshToken");
        verify(userClient).updateUserLastLogin(1L);
    }

    @Test
    void reissueTokens() {
        ReissueRequest request = new ReissueRequest(1L, UserType.USER, "refreshToken");
        when(tokenRepository.getRefreshToken(1L, UserType.USER)).thenReturn("refreshToken");
        Claims claims = mock(Claims.class);
        when(jwtTokenProvider.parseToken("refreshToken")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("username");

        AuthResponse user = new AuthResponse(1L, "username", "encodedPassword");
        when(userClient.getUser("username")).thenReturn(CommonResponse.success(user));

        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), any()))
                .thenReturn(new JwtToken("newAccessToken", 1L));
        when(jwtTokenProvider.generateRefreshToken(anyLong(), anyString(), any()))
                .thenReturn(new JwtToken("newRefreshToken", 2L));

        LoginResponse response = tokenService.reissueTokens(request);

        assertThat(response.accessToken().token()).isEqualTo("newAccessToken");
        assertThat(response.accessToken().expiresAt()).isEqualTo(1L);
        assertThat(response.refreshToken().token()).isEqualTo("newRefreshToken");
        assertThat(response.refreshToken().expiresAt()).isEqualTo(2L);
    }

    @Test
    void reissueTokensWithInvalidRefreshToken() {
        ReissueRequest request = new ReissueRequest(1L, UserType.USER, "invalidRefreshToken");
        when(tokenRepository.getRefreshToken(1L, UserType.USER)).thenReturn("refreshToken");

        assertThatThrownBy(() -> tokenService.reissueTokens(request)).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void reissueTokensWithSavedRefreshTokenNotFound() {
        ReissueRequest request = new ReissueRequest(1L, UserType.USER, "invalidRefreshToken");
        when(tokenRepository.getRefreshToken(1L, UserType.USER)).thenReturn(null);

        assertThatThrownBy(() -> tokenService.reissueTokens(request)).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void invalidateTokens() {
        Claims claims = mock(Claims.class);
        when(jwtTokenProvider.parseToken("accessToken")).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(1L);
        when(claims.get("userType", String.class)).thenReturn("USER");

        tokenService.invalidateTokens("accessToken");

        verify(tokenRepository).saveAccessTokenToBlackList("accessToken");
        verify(tokenRepository).deleteRefreshToken(1L, UserType.USER);
    }
}
