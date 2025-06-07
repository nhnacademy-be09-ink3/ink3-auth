package shop.ink3.auth.client.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserStatus {
    ACTIVE,
    DORMANT,
    WITHDRAWN
}
