package shop.ink3.auth.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class KeyUtilsTest {

    @Test
    void publicKeyToPem() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();

        String pem = KeyUtils.publicKeyToPem(publicKey);

        assertThat(pem).startsWith("-----BEGIN PUBLIC KEY-----\n");
        assertThat(pem).endsWith("-----END PUBLIC KEY-----");

        String base64Body = pem
                .replace("-----BEGIN PUBLIC KEY-----\n", "")
                .replace("\n-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(base64Body);

        assertThat(decoded).isEqualTo(publicKey.getEncoded());
    }
}
