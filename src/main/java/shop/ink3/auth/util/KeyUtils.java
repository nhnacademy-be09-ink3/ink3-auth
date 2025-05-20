package shop.ink3.auth.util;

import java.security.PublicKey;
import java.util.Base64;

public class KeyUtils {
    private KeyUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String publicKeyToPem(PublicKey publicKey) {
        String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String body = base64.replaceAll("(.{64})", "$1\n");
        return "-----BEGIN PUBLIC KEY-----\n" + body + "\n-----END PUBLIC KEY-----";
    }
}
