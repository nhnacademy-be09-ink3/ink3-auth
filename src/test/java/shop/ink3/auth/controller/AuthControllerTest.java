package shop.ink3.auth.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.ink3.auth.dto.JwtToken;
import shop.ink3.auth.dto.LoginRequest;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.LogoutRequest;
import shop.ink3.auth.dto.ReissueRequest;
import shop.ink3.auth.dto.UserType;
import shop.ink3.auth.exception.DormantException;
import shop.ink3.auth.exception.InvalidPasswordException;
import shop.ink3.auth.exception.InvalidRefreshTokenException;
import shop.ink3.auth.exception.UserNotFoundException;
import shop.ink3.auth.exception.WithdrawnException;
import shop.ink3.auth.oauth.service.OAuth2Service;
import shop.ink3.auth.service.AuthService;
import shop.ink3.auth.service.TokenService;
import shop.ink3.auth.util.KeyUtils;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    PublicKey publicKey;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    OAuth2Service oAuth2Service;

    @Test
    void getPublicKey() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

        when(publicKey.getEncoded()).thenReturn(publicKeyBytes);

        mockMvc.perform(get("/public-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.publicKey").value(KeyUtils.publicKeyToPem(keyPair.getPublic())))
                .andDo(print());
    }

    @Test
    void login() throws Exception {
        LoginRequest request = new LoginRequest("username", "password", UserType.USER);
        JwtToken accessToken = new JwtToken("accessToken", 1L);
        JwtToken refreshToken = new JwtToken("refreshToken", 2L);
        LoginResponse response = new LoginResponse(accessToken, refreshToken);

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.accessToken.token").value("accessToken"))
                .andExpect(jsonPath("$.data.accessToken.expiresAt").value(1L))
                .andExpect(jsonPath("$.data.refreshToken.token").value("refreshToken"))
                .andExpect(jsonPath("$.data.refreshToken.expiresAt").value(2L))
                .andDo(print());
    }

    @Test
    void loginWithInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("username", "invalidPassword", UserType.USER);
        when(authService.login(request)).thenThrow(new InvalidPasswordException());
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andDo(print());
    }

    @Test
    void loginWithUserNotFound() throws Exception {
        LoginRequest request = new LoginRequest("username", "password", UserType.USER);
        when(authService.login(request)).thenThrow(new UserNotFoundException());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andDo(print());
    }

    @Test
    void loginWithDormantUser() throws Exception {
        LoginRequest request = new LoginRequest("username", "password", UserType.USER);
        when(authService.login(request)).thenThrow(new DormantException());
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.LOCKED.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andDo(print());
    }

    @Test
    void loginWithWithDrawnUser() throws Exception {
        LoginRequest request = new LoginRequest("username", "password", UserType.USER);
        when(authService.login(request)).thenThrow(new WithdrawnException());
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGone())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.GONE.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andDo(print());
    }

    @Test
    void reissue() throws Exception {
        ReissueRequest request = new ReissueRequest(1L, UserType.USER, "refreshToken");
        JwtToken newAccessToken = new JwtToken("newAccessToken", 1L);
        JwtToken newRefreshToken = new JwtToken("newRefreshToken", 2L);
        LoginResponse response = new LoginResponse(newAccessToken, newRefreshToken);
        when(tokenService.reissueTokens(request)).thenReturn(response);

        mockMvc.perform(post("/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.accessToken.token").value("newAccessToken"))
                .andExpect(jsonPath("$.data.accessToken.expiresAt").value(1L))
                .andExpect(jsonPath("$.data.refreshToken.token").value("newRefreshToken"))
                .andExpect(jsonPath("$.data.refreshToken.expiresAt").value(2L))
                .andDo(print());
    }

    @Test
    void reissueWithInvalidToken() throws Exception {
        ReissueRequest request = new ReissueRequest(1L, UserType.USER, "InvalidRefreshToken");
        when(tokenService.reissueTokens(request)).thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andDo(print());
    }

    @Test
    void reissueWithExpiredToken() throws Exception {
        ReissueRequest request = new ReissueRequest(1L, UserType.USER, "InvalidRefreshToken");
        when(tokenService.reissueTokens(request)).thenThrow(new ExpiredJwtException(null, null, "Token expired."));

        mockMvc.perform(post("/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andDo(print());
    }

    @Test
    void reissueWithUserNotFound() throws Exception {
        ReissueRequest request = new ReissueRequest(1L, UserType.USER, "refreshToken");
        when(tokenService.reissueTokens(request)).thenThrow(new UserNotFoundException());
        mockMvc.perform(post("/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andDo(print());
    }

    @Test
    void reissueWithDormantUser() throws Exception {
        ReissueRequest request = new ReissueRequest(1L, UserType.USER, "refreshToken");
        when(tokenService.reissueTokens(request)).thenThrow(new DormantException());
        mockMvc.perform(post("/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.LOCKED.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andDo(print());
    }

    @Test
    void reissueWithWithDrawnUser() throws Exception {
        ReissueRequest request = new ReissueRequest(1L, UserType.USER, "refreshToken");
        when(tokenService.reissueTokens(request)).thenThrow(new WithdrawnException());
        mockMvc.perform(post("/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGone())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.GONE.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andDo(print());
    }

    @Test
    void logout() throws Exception {
        LogoutRequest request = new LogoutRequest("accessToken");
        doNothing().when(authService).logout("accessToken");

        mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}
