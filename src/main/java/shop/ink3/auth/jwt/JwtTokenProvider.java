package shop.ink3.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    public String generateAccessToken(long id, String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("id", id)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenValidity))
                .signWith(privateKey, SIG.RS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenValidity))
                .signWith(privateKey, SIG.RS256)
                .compact();
    }

    public Claims parseToken(String refreshToken) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();
    }
}
