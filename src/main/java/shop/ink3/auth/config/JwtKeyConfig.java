package shop.ink3.auth.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtKeyConfig {
    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Bean
    public PrivateKey privateKey() throws Exception {
        String key = readKey(privateKeyPath);
        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        String key = readKey(publicKeyPath);
        key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    public String readKey(String keyPath) throws IOException {
        if (keyPath.startsWith("classpath:")) {
            String pathInClasspath = keyPath.replace("classpath:", "");
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(pathInClasspath)) {
                if (Objects.isNull(is)) {
                    throw new FileNotFoundException("Cannot find classpath resource: " + pathInClasspath);
                }
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } else {
            return Files.readString(Paths.get(keyPath));
        }
    }
}
