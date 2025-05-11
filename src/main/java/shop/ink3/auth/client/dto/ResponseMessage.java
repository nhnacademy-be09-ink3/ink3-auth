package shop.ink3.auth.client.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResponseMessage {
    SUCCESS("Request processed successfully.");
    private final String message;
}
