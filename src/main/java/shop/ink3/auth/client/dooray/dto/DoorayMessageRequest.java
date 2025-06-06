package shop.ink3.auth.client.dooray.dto;

import java.util.List;

public record DoorayMessageRequest(
        String botName,
        String text,
        List<Attachment> attachments
) {
}
