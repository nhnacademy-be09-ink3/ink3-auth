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
import shop.ink3.auth.dto.JwtToken;
import shop.ink3.auth.dto.UserType;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    public JwtToken generateAccessToken(long id, String username, UserType userType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity);
        String token = Jwts.builder()
                .subject(username)
                .claim("id", id)
                .claim("userType", userType.name())
                .claim("tokenType", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, SIG.RS256)
                .compact();
        return new JwtToken(token, expiry.getTime());
    }

    public JwtToken generateRefreshToken(long id, String username, UserType userType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidity);
        String token = Jwts.builder()
                .subject(username)
                .claim("id", id)
                .claim("userType", userType.name())
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, SIG.RS256)
                .compact();
        return new JwtToken(token, expiry.getTime());
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
