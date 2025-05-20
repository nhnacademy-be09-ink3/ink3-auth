package shop.ink3.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.ink3.auth.client.UserClient;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.dto.LoginRequest;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.UserRole;
import shop.ink3.auth.exception.InvalidPasswordException;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginResponse login(LoginRequest request) {
        AuthResponse user = (request.role() == UserRole.ADMIN ? userClient.getAdmin(request.username()) : userClient.getUser(request.username())).data();

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new InvalidPasswordException();
        }

        if (request.role() == UserRole.ADMIN) {
            userClient.updateAdminLastLogin(user.id());
        }
        else {
            userClient.updateUserLastLogin(user.id());
        }

        return tokenService.issueTokens(user, request.role());
    }

    public void logout(String accessToken) {
        tokenService.invalidateTokens(accessToken);
    }
}
