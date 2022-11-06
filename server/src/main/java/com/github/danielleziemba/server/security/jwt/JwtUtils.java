package com.github.danielleziemba.server.security.jwt;

import com.github.danielleziemba.server.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${bugtracker.app.jwtSecret}")
    private String jwtSecret;

    @Value("${bugtracker.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${bugtracker.app.jwtBase64Secret}")
    private String base64Secret;

    private Key key;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64URL.decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}" + e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}" + e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}" + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}" + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}" + e.getMessage());
        }

        return false;
    }

}
