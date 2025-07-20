package com.beautiflow.global.common.util;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.security.CustomOAuth2User;
import com.beautiflow.global.domain.GlobalRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
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

    public String getProvider(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("provider", String.class);
        } catch (ExpiredJwtException e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }
    }

    public String getKakaoId(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("kakaoId", String.class);
        } catch (ExpiredJwtException e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }
    }

    public Long getUserId(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("userId", Number.class)
                    .longValue();
        } catch (ExpiredJwtException e) {
            System.out.println("2");
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_EXPIRED);
        } catch (Exception e) {
            System.out.println("3");
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }
    }

    public Boolean isExpired(String token) {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());

    }


    public boolean validateToken(String token) {
        return !isExpired(token);
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

    public Optional<Claims> checkRefreshToken(String refreshToken, String email) throws BeautiFlowException{
        String redisRefreshToken = redisTokenUtil.getValues(email);
        System.out.println(redisRefreshToken);
        if (!refreshToken.equals(redisRefreshToken)) {
            throw new BeautiFlowException(UserErrorCode.TOKEN_GENERATION_FAILED);
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
            return Optional.of(claims);
        }catch (ExpiredJwtException e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.DATABASE_ERROR);
        }
    }
}
