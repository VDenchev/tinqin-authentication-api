package com.tinqinacademy.authentication.core.providers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtProvider {

  @Value("${security.jwt.secret-key}")
  private String secretKey;
  @Value("${security.jwt.duration-time}")
  private Long durationTime;

  public String createToken(String username) {
    Date currentTime = new Date();
    Date expireTime = new Date(currentTime.getTime() + this.durationTime);

    return Jwts.builder()
        .subject(username)
        .issuedAt(currentTime)
        .expiration(expireTime)
        .signWith(getKey())
        .compact();
  }

  public String getUsernameFromToken(String token) {
    return extractClaims(token).getSubject();
  }

  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }
}
