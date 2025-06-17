package shop.ink3.auth.oauth.dto;

import java.io.Serializable;

public record OAuth2UserInfo(
        String provider,
        String providerId,
        String name,
        String gender,
        String email,
        String mobile,
        String birthday
) implements Serializable {
}
