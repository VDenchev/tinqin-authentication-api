package com.tinqinacademy.authentication.core.providers;

import com.tinqinacademy.authentication.api.enums.RoleEnum;
import com.tinqinacademy.authentication.api.exceptions.JwtException;
import com.tinqinacademy.authentication.api.services.base.TokenProvider;
import com.tinqinacademy.authentication.persistence.mongorepositories.InvalidatedJwtRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.EMPTY_JWT_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.INVALID_JWT_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.INVALID_SIGNATURE_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.JWT_EXPIRED_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.PARSING_JWT_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.UNSUPPORTED_JWT_MESSAGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider implements TokenProvider {

  @Value("${security.jwt.secret-key}")
  private String secretKey;
  @Value("${security.jwt.duration-time}")
  private Long durationTime;

  private final InvalidatedJwtRepository invalidatedJwtRepository;

  public String createToken(String username, List<RoleEnum> roles) {
    Instant issuedAt = Instant.now();
    Instant expiration = issuedAt.plusMillis(durationTime);

    return Jwts.builder()
        .claim("username", username)
        .claim("roles", roles)
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(expiration))
        .signWith(getKey())
        .compact();
  }

  public String getUsernameFromToken(String token) {
    return extractClaims(token).get("username", String.class);
  }

  public Instant getExpirationTimeFromToken(String token) {
    return extractClaims(token).getExpiration().toInstant();
  }

  public List<RoleEnum> getRolesFromToken(String token) {
    List<String> rolesAsString = extractClaims(token).get("roles", ArrayList.class);
    return rolesAsString.stream()
        .map(RoleEnum::valueOf)
        .toList();
  }

  @Override
  public void validate(String token) throws JwtException {
    extractClaims(token);

    boolean isTokenInvalidated = invalidatedJwtRepository.existsByToken(token);
    if (isTokenInvalidated) {
      throw new JwtException(JWT_EXPIRED_MESSAGE);
    }
  }

  private Claims extractClaims(String token) throws JwtException {
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
    } catch (SignatureException e) {
      throw new JwtException(INVALID_SIGNATURE_MESSAGE);
    } catch (io.jsonwebtoken.JwtException e) {
      throw new JwtException(PARSING_JWT_MESSAGE);
    } catch (IllegalArgumentException e) {
      throw new JwtException(EMPTY_JWT_MESSAGE);
    } catch (Exception e) {
      throw new JwtException(PARSING_JWT_MESSAGE);
    }
  }

  private SecretKey getKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }
}
