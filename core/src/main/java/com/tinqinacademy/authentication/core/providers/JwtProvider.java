package com.tinqinacademy.authentication.core.providers;

import com.tinqinacademy.authentication.api.enums.RoleEnum;
import com.tinqinacademy.authentication.api.services.base.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class JwtProvider implements TokenProvider {

  @Value("${security.jwt.secret-key}")
  private String secretKey;
  @Value("${security.jwt.duration-time}")
  private Long durationTime;

  public String createToken(String username, List<RoleEnum> roles) {
    Date currentTime = new Date();
    Date expireTime = new Date(currentTime.getTime() + this.durationTime);

    return Jwts.builder()
        .claim("username", username)
        .claim("roles", roles)
        .issuedAt(currentTime)
        .expiration(expireTime)
        .signWith(getKey())
        .compact();
  }

  public String getUsernameFromToken(String token) {
    return extractClaims(token).get("username", String.class);
  }

  public List<RoleEnum> getRolesFromToken(String token) {
    return extractClaims(token).get("roles", ArrayList.class);
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
