package shop.ink3.auth.service;

import io.jsonwebtoken.Claims;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.ink3.auth.client.UserClient;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.dto.LoginRequest;
import shop.ink3.auth.dto.ReissueRequest;
import shop.ink3.auth.dto.UserType;
import shop.ink3.auth.exception.InvalidPasswordException;
import shop.ink3.auth.exception.InvalidRefreshTokenException;
import shop.ink3.auth.jwt.JwtTokenProvider;
import shop.ink3.auth.repository.TokenRepository;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;

    public shop.ink3.auth.dto.AuthResponse login(LoginRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) loadUserByUsername(request.username(), request.type());

        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            throw new InvalidPasswordException();
        }

        if (request.type() == UserType.ADMIN) {
            userClient.updateAdminLastLogin(userDetails.getId());
        }
        else {
            userClient.updateUserLastLogin(userDetails.getId());
        }

        return issueTokens(userDetails, request.type());
    }

    public shop.ink3.auth.dto.AuthResponse reissue(ReissueRequest request) {
        String savedRefreshToken = tokenRepository.getRefreshToken(request.id(), request.type());

        if (Objects.isNull(savedRefreshToken) || !savedRefreshToken.equals(request.refreshToken())) {
            throw new InvalidRefreshTokenException();
        }

        String username = jwtTokenProvider.parseToken(request.refreshToken()).getSubject();

        CustomUserDetails userDetails = (CustomUserDetails) loadUserByUsername(username, request.type());

        return issueTokens(userDetails, request.type());
    }

    public void logout(String accessToken) {
        Claims claims = jwtTokenProvider.parseToken(accessToken);
        long id = claims.get("id", Long.class);
        UserType type = UserType.valueOf(
                claims.get("role", String.class).toUpperCase().replace("ROLE_", "")
        );
        tokenRepository.saveAccessTokenToBlackList(accessToken);
        tokenRepository.deleteRefreshToken(id, type);
    }

    private UserDetails loadUserByUsername(String username, UserType type) throws UsernameNotFoundException {
        CommonResponse<AuthResponse> response = type == UserType.ADMIN ? userClient.getAdmin(username) : userClient.getUser(username);
        return new CustomUserDetails(response.data().id(), response.data().username(), response.data().password(), type.name());
    }

    private shop.ink3.auth.dto.AuthResponse issueTokens(CustomUserDetails userDetails, UserType type) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                userDetails.getUsername()
        );

        tokenRepository.saveRefreshToken(userDetails.getId(), type, refreshToken);
        return new shop.ink3.auth.dto.AuthResponse(accessToken, refreshToken);
    }
}
