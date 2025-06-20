package shop.ink3.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.PublicKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.dto.LoginRequest;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.LogoutRequest;
import shop.ink3.auth.dto.PublicKeyResponse;
import shop.ink3.auth.dto.ReissueRequest;
import shop.ink3.auth.dto.SendReactiveCodeRequest;
import shop.ink3.auth.dto.VerifyReactiveCodeRequest;
import shop.ink3.auth.oauth.service.OAuth2Service;
import shop.ink3.auth.service.AuthService;
import shop.ink3.auth.service.ReactiveService;
import shop.ink3.auth.service.TokenService;
import shop.ink3.auth.util.CookieUtil;
import shop.ink3.auth.util.KeyUtils;

@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;
    private final TokenService tokenService;
    private final OAuth2Service oAuth2Service;
    private final ReactiveService reactiveService;
    private final PublicKey publicKey;

    @Value("${front.url}")
    private String frontUrl;

    @GetMapping("/public-key")
    public ResponseEntity<CommonResponse<PublicKeyResponse>> getPublicKey() {
        return ResponseEntity.ok(CommonResponse.success(new PublicKeyResponse(KeyUtils.publicKeyToPem(publicKey))));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<LoginResponse>> reissue(@RequestBody @Valid ReissueRequest request) {
        LoginResponse response = tokenService.reissueTokens(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest request) {
        authService.logout(request.accessToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/oauth2/authorization/{provider}")
    public ResponseEntity<Void> getAuthorization(@PathVariable String provider) {
        URI uri = oAuth2Service.getAuthorizationUri(provider);
        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }

    @GetMapping("/oauth2/callback/{provider}")
    public ResponseEntity<Void> oauth2Callback(
            @PathVariable String provider,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        LoginResponse tokens = oAuth2Service.processOAuth2Callback(provider, request);
        URI uri = UriComponentsBuilder.fromUriString(frontUrl).build().toUri();
        CookieUtil.setTokenCookies(response, tokens.accessToken(), tokens.refreshToken());
        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }

    @PostMapping("/reactivate/send-code")
    public ResponseEntity<Void> sendReactivateCode(@RequestBody @Valid SendReactiveCodeRequest request) {
        reactiveService.sendReactiveCode(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reactivate/verify")
    public ResponseEntity<Void> verifyReactiveCode(@RequestBody @Valid VerifyReactiveCodeRequest request) {
        reactiveService.reactivateUser(request);
        return ResponseEntity.noContent().build();
    }
}
