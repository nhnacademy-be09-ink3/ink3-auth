package shop.ink3.auth.service;

import io.jsonwebtoken.Claims;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.ink3.auth.client.UserClient;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.dto.JwtToken;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.ReissueRequest;
import shop.ink3.auth.dto.UserType;
import shop.ink3.auth.exception.InvalidRefreshTokenException;
import shop.ink3.auth.jwt.JwtTokenProvider;
import shop.ink3.auth.repository.TokenRepository;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final UserClient userClient;

    public LoginResponse issueTokens(AuthResponse user, UserType userType) {
        JwtToken accessToken = jwtTokenProvider.generateAccessToken(
                user.id(),
                user.username(),
                userType
        );
        JwtToken refreshToken = jwtTokenProvider.generateRefreshToken(
                user.id(),
                user.username(),
                userType
        );

        tokenRepository.saveRefreshToken(user.id(), userType, refreshToken.token());
        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse reissueTokens(ReissueRequest request) {
        String savedRefreshToken = tokenRepository.getRefreshToken(request.id(), request.userType());

        if (Objects.isNull(savedRefreshToken) || !savedRefreshToken.equals(request.refreshToken())) {
            throw new InvalidRefreshTokenException();
        }

        String username = jwtTokenProvider.parseToken(request.refreshToken()).getSubject();

        AuthResponse user = (request.userType() == UserType.ADMIN ? userClient.getAdmin(username)
                : userClient.getUser(username)).data();

        return issueTokens(user, request.userType());
    }

    public void invalidateTokens(String accessToken) {
        Claims claims = jwtTokenProvider.parseToken(accessToken);
        long id = claims.get("id", Long.class);
        UserType userType = UserType.valueOf(claims.get("userType", String.class).toUpperCase());
        tokenRepository.saveAccessTokenToBlackList(accessToken);
        tokenRepository.deleteRefreshToken(id, userType);
    }
}
