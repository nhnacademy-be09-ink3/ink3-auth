package shop.ink3.auth.controller;

import java.security.PublicKey;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.dto.LoginRequest;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.LogoutRequest;
import shop.ink3.auth.dto.PublicKeyResponse;
import shop.ink3.auth.dto.ReissueRequest;
import shop.ink3.auth.service.AuthService;
import shop.ink3.auth.service.TokenService;
import shop.ink3.auth.util.KeyUtils;

@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;
    private final TokenService tokenService;
    private final PublicKey publicKey;

    @GetMapping("/public-key")
    public ResponseEntity<CommonResponse<PublicKeyResponse>> getPublicKey() {
        return ResponseEntity.ok(CommonResponse.success(new PublicKeyResponse(KeyUtils.publicKeyToPem(publicKey))));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<LoginResponse>> reissue(@RequestBody ReissueRequest request) {
        LoginResponse response = tokenService.reissueTokens(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.accessToken());
        return ResponseEntity.noContent().build();
    }
}
