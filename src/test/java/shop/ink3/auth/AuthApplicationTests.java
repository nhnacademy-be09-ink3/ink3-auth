package shop.ink3.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class AuthApplicationTests {
    @MockitoBean
    ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    OAuth2AuthorizedClientService authorizedClientService;

    @Test
    void contextLoads() {
    }
}
