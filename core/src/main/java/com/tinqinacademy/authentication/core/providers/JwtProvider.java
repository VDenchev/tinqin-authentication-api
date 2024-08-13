package com.tinqinacademy.authentication.core.providers;

import com.tinqinacademy.authentication.api.enums.RoleEnum;
import com.tinqinacademy.authentication.api.exceptions.JwtException;
import com.tinqinacademy.authentication.api.services.base.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.EMPTY_JWT_EXCEPTION;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.INVALID_JWT_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.JWT_EXPIRED_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.PARSING_JWT_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.UNSUPPORTED_JWT_MESSAGE;

@Slf4j
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
    try {
      return Jwts.parser()
          .verifyWith(getKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw new JwtException(JWT_EXPIRED_MESSAGE);
    } catch (MalformedJwtException e) {
      throw new JwtException(INVALID_JWT_MESSAGE);
    } catch (UnsupportedJwtException e) {
      throw new JwtException(UNSUPPORTED_JWT_MESSAGE);
    } catch (io.jsonwebtoken.JwtException e) {
      throw new JwtException(PARSING_JWT_MESSAGE);
    } catch (IllegalArgumentException e) {
      throw new JwtException(EMPTY_JWT_EXCEPTION);
    }
  }

  private SecretKey getKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }
}
