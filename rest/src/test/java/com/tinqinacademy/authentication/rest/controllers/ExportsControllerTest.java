package com.tinqinacademy.authentication.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqinacademy.authentication.core.providers.JwtProvider;
import com.tinqinacademy.authentication.persistence.entities.Role;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.enums.RoleEnum;
import com.tinqinacademy.authentication.persistence.mongorepositories.InvalidatedJwtRepository;
import com.tinqinacademy.authentication.persistence.mongorepositories.RecoveryCodeRepository;
import com.tinqinacademy.authentication.persistence.mongorepositories.VerificationCodeRepository;
import com.tinqinacademy.authentication.persistence.repositories.RoleRepository;
import com.tinqinacademy.authentication.persistence.repositories.UserRepository;
import com.tinqinacademy.authentication.rest.AuthenticationApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.SEARCH_USERS;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.VALIDATE_TOKEN;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AuthenticationApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExportsControllerTest extends BaseControllerTest{

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private JwtProvider jwtProvider;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private InvalidatedJwtRepository invalidatedJwtRepository;
  @Autowired
  private RecoveryCodeRepository recoveryCodeRepository;
  @Autowired
  private VerificationCodeRepository verificationCodeRepository;

  @BeforeEach
  void setUp() {
    Role userRole = roleRepository.findByType(RoleEnum.USER).orElseThrow();
    Role adminRole = roleRepository.findByType(RoleEnum.ADMIN).orElseThrow();
    User admin = User.builder()
        .email("test.email@example.com")
        .username("testUsername")
        //hashed password=testpassword
        .password("$argon2id$v=19$m=20000,t=2,p=1$yH9HK7yY8CTaMYjMIqJ0dA$upius4BwkJ1hwHG/h08GbotUmJfBUHq35iiNijGKoX4")
        .firstName("Test")
        .lastName("Testov")
        .phoneNumber("+359 123456789")
        .isVerified(true)
        .roles(List.of(userRole, adminRole))
        .build();

    userRepository.save(admin);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
    verificationCodeRepository.deleteAll();
    invalidatedJwtRepository.deleteAll();
    recoveryCodeRepository.deleteAll();
  }

  @Test
  void validateToken_whenValidToken_shouldRespondWithOkAndReturnUserDetails() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    mockMvc.perform(post(VALIDATE_TOKEN)
            .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.username", is("testUsername")));
  }

  @Test
  void validateToken_whenInvalidToken_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = "notavalidtoken";

    mockMvc.perform(post(VALIDATE_TOKEN)
            .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid JWT format")))
        .andExpect(jsonPath("$.statusCode", is("BAD_REQUEST")))
    ;
  }

  @Test
  void searchUsers_whenCorrectInput_shouldRespondWithOk() throws Exception {
    mockMvc.perform(get(SEARCH_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .param("phoneNo", "+359")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userIds").isNotEmpty());
  }

  @Test
  void searchUsers_whenWrongInput_shouldRespondWithOk() throws Exception {
    mockMvc.perform(get(SEARCH_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .param("phoneNo", "notaphonenumber")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userIds").isEmpty());
  }
}