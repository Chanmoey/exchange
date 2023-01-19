package com.moon.exchange.counter.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@PropertySource(value = "classpath:config/security.properties")
public class JwtToken {

    private static String jwtKey;
    private static Integer expiredTimeIn;

    @Value("${security.jwt-key}")
    public void setJwtKey(String jwtKey) {
        JwtToken.jwtKey = jwtKey;
    }

    @Value("${security.token-expired-in}")
    public void setExpiredTimeIn(Integer expiredTimeIn) {
        JwtToken.expiredTimeIn = expiredTimeIn;
    }

    public static Optional<Map<String, Claim>> getClaims(String token) {
        DecodedJWT decodedJWT;
        Algorithm algorithm = Algorithm.HMAC256(JwtToken.jwtKey);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            decodedJWT = jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
        return Optional.of(decodedJWT.getClaims());
    }

    public static Boolean verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(JwtToken.jwtKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
        } catch (TokenExpiredException e) {
            throw e;
        } catch (JWTVerificationException e) {
            return false;
        }
        return true;
    }


    public static String makeToken(Long uid) {
        return JwtToken.getToken(uid);
    }

    private static String getToken(Long uid) {
        Algorithm algorithm = Algorithm.HMAC256(JwtToken.jwtKey);
        Map<String, Date> map = JwtToken.calculateExpiredIssues();

        return JWT.create()
                .withClaim("uid", uid)
                .withExpiresAt(map.get("expiredTime"))
                .withIssuedAt(map.get("now"))
                .sign(algorithm);
    }

    private static Map<String, Date> calculateExpiredIssues() {
        Map<String, Date> map = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.SECOND, JwtToken.expiredTimeIn);
        map.put("now", now);
        map.put("expiredTime", calendar.getTime());
        return map;
    }
}