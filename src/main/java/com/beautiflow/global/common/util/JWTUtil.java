package com.beautiflow.global.common.util;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.global.domain.GlobalRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    @Value("${spring.jwt.validity.access}")
    private Long accessTokenValidity;
    @Value("${spring.jwt.validity.refresh}")
    private Long refreshTokenValidity;
    private final RedisTokenUtil redisTokenUtil;


    private SecretKey secretKey;

    public JWTUtil(RedisTokenUtil redisTokenUtil, @Value("${spring.jwt.secret}") String secret) {
        this.redisTokenUtil = redisTokenUtil;
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }


    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtException(UserErrorCode.JWT_TOKEN_EXPIRED.getMessage());
        } catch (Exception e) {
            throw new JwtException(UserErrorCode.JWT_TOKEN_INVALID.getMessage());
        }
    }


    private <T> T getClaim(String token, String key, Class<T> clazz) {
        return parseClaims(token).get(key, clazz);
    }

    public String getProvider(String token) {
        return getClaim(token, "provider", String.class);
    }

    public String getKakaoId(String token) {
        return getClaim(token, "kakaoId", String.class);
    }

    public Long getUserId(String token) {
        return getClaim(token, "userId", Number.class).longValue();
    }

    public boolean validateToken(String token) {
        parseClaims(token);
        return true;
    }

    //액세스토큰 만료되었으면 true반환
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }
    }


    public Claims parseRefresh(String refreshToken) {
        // Claims 파싱 및 토큰 유효성 검사
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BeautiFlowException(UserErrorCode.REFRESH_ALSO_EXPIRED);
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }

        // Redis저장값과 비교
        Long userId = claims.get("userId", Number.class).longValue();
        String redisKey = "refresh:" + userId;
        String storedToken = redisTokenUtil.getValues(redisKey);

        if (!refreshToken.equals(storedToken)) {
            throw new BeautiFlowException(UserErrorCode.TOKEN_GENERATION_FAILED);
        }

        return claims;
    }


    public Authentication getAuthentication(String token) {
        String provider = getProvider(token);
        String kakaoId = getKakaoId(token);
        Long userId = getUserId(token);

        GlobalRole globalRole = switch (provider) {
            case "kakao-customer" -> GlobalRole.CUSTOMER;
            case "kakao-staff" -> GlobalRole.STAFF;
            default -> throw new RuntimeException("Invalid provider");
        };

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(provider, kakaoId, userId, globalRole);

        return new UsernamePasswordAuthenticationToken(customOAuth2User, userId, customOAuth2User.getAuthorities());
    }


    public String createAccessToken(String provider, String kakaoId, Long userId) {

        return Jwts.builder()
                .claim("provider", provider)
                .claim("kakaoId", kakaoId)
                .claim("userId", userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String kakaoId, Long userId) {
        String refreshToken = Jwts.builder()
                .claim("kakaoId", kakaoId)
                .claim("userId", userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(secretKey)
                .compact();
        redisTokenUtil.setValues("refresh:" + userId, refreshToken, Duration.ofMillis(refreshTokenValidity));

        return refreshToken;


    }
}
