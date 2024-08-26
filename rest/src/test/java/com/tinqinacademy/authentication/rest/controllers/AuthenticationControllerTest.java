package com.tinqinacademy.authentication.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqinacademy.authentication.api.operations.changepassword.input.ChangePasswordInput;
import com.tinqinacademy.authentication.api.operations.changepasswordusingrecoverycode.input.ChangePasswordUsingRecoveryCodeInput;
import com.tinqinacademy.authentication.api.operations.confirmregistration.input.ConfirmRegistrationInput;
import com.tinqinacademy.authentication.api.operations.demote.input.DemoteInput;
import com.tinqinacademy.authentication.api.operations.login.input.LoginInput;
import com.tinqinacademy.authentication.api.operations.promote.input.PromoteInput;
import com.tinqinacademy.authentication.api.operations.recoverpassword.input.RecoverPasswordInput;
import com.tinqinacademy.authentication.api.operations.register.input.RegisterInput;
import com.tinqinacademy.authentication.api.services.base.MailService;
import com.tinqinacademy.authentication.core.providers.JwtProvider;
import com.tinqinacademy.authentication.persistence.entities.RecoveryCode;
import com.tinqinacademy.authentication.persistence.entities.Role;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.entities.VerificationCode;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.CHANGE_PASSWORD;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.CHANGE_PASSWORD_USING_RECOVERY;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.CONFIRM_REGISTRATION;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.DEMOTE;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.LOGIN;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.LOGOUT;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.PROMOTE;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.RECOVER_PASSWORD;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.REGISTER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AuthenticationApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest extends BaseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MailService mailService;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private JwtProvider jwtProvider;
  @Autowired
  private InvalidatedJwtRepository invalidatedJwtRepository;
  @Autowired
  private VerificationCodeRepository verificationCodeRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private RecoveryCodeRepository recoveryCodeRepository;

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

    User user = User.builder()
        .email("user@example.com")
        .username("user123")
        //hashed password=testpassword
        .password("$argon2id$v=19$m=20000,t=2,p=1$yH9HK7yY8CTaMYjMIqJ0dA$upius4BwkJ1hwHG/h08GbotUmJfBUHq35iiNijGKoX4")
        .firstName("User")
        .lastName("Userov")
        .phoneNumber("+359 555333111")
        .isVerified(true)
        .roles(List.of(userRole))
        .build();

    User admin1 = User.builder()
        .email("admin@example.com")
        .username("admin123")
        //hashed password=testpassword
        .password("$argon2id$v=19$m=20000,t=2,p=1$yH9HK7yY8CTaMYjMIqJ0dA$upius4BwkJ1hwHG/h08GbotUmJfBUHq35iiNijGKoX4")
        .firstName("Admin")
        .lastName("Adminov")
        .phoneNumber("+359 333222111")
        .isVerified(true)
        .roles(List.of(userRole, adminRole))
        .build();
    userRepository.save(admin);
    userRepository.save(user);
    userRepository.save(admin1);

    doNothing().when(mailService).sendEmail(anyString(), anyString(), anyMap(), anyString());
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
    verificationCodeRepository.deleteAll();
    invalidatedJwtRepository.deleteAll();
    recoveryCodeRepository.deleteAll();
  }

  @Test
  void login_whenCorrectUsernameAndPassword_shouldRespondWithOkResponseAndReturnJwt() throws Exception {
    LoginInput input = LoginInput.builder()
        .username("testUsername")
        .password("testpassword")
        .build();

    mockMvc.perform(post(LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isOk())
        .andExpect(header().exists("Authorization"));

  }

  @Test
  void login_whenWrongUsername_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    LoginInput input = LoginInput.builder()
        .username("WrongUsername")
        .password("testpassword")
        .build();

    mockMvc.perform(post(LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isBadRequest())
        .andExpect(header().doesNotExist("Authorization"))
        .andExpect(jsonPath("$.errors[0].message", is("Invalid credentials")))
        .andExpect(jsonPath("$.statusCode", is("BAD_REQUEST")));
  }

  @Test
  void login_whenWrongPassword_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    LoginInput input = LoginInput.builder()
        .username("testUsername")
        .password("wrongpassword")
        .build();

    mockMvc.perform(post(LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isBadRequest())
        .andExpect(header().doesNotExist("Authorization"))
        .andExpect(jsonPath("$.errors[0].message", is("Invalid credentials")))
        .andExpect(jsonPath("$.statusCode", is("BAD_REQUEST")));
  }

  @Test
  void login_whenNullUsername_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    LoginInput input = LoginInput.builder()
        .password("testpassword")
        .build();

    mockMvc.perform(post(LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(header().doesNotExist("Authorization"))
        .andExpect(jsonPath("$.errors[0].message", is("Username cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("username")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void login_whenBlankUsername_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    LoginInput input = LoginInput.builder()
        .username("")
        .password("testpassword")
        .build();

    mockMvc.perform(post(LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(header().doesNotExist("Authorization"))
        .andExpect(jsonPath("$.errors[0].message", is("Username cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("username")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void login_whenNullPassword_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    LoginInput input = LoginInput.builder()
        .username("testUsername")
        .build();

    mockMvc.perform(post(LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(header().doesNotExist("Authorization"))
        .andExpect(jsonPath("$.errors[0].message", is("Password cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("password")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void login_whenBlankPassword_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    LoginInput input = LoginInput.builder()
        .username("testUsername")
        .password("")
        .build();

    mockMvc.perform(post(LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(header().doesNotExist("Authorization"))
        .andExpect(jsonPath("$.errors[0].message", is("Password cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("password")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void logout_whenValidToken_shouldRespondWithOk() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));
    mockMvc.perform(post(LOGOUT)
            .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

    assertTrue(invalidatedJwtRepository.existsByToken(token));
  }

  @Test
  void logout_whenExpiredToken_shouldRespondWithUnauthorized() throws Exception {
    String expiredToken = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VybmFtZSI6InRlc3RVc2VybmFtZSIsInJvbGVzIjpbIkFETUlOIiwiVVNFUiJdLCJpYXQiOjE3MjQ1Mjc5NzQsImV4cCI6MTcyNDUyODI3NH0.qrTjTWZ4aCU5mW4x2hIpCGKPQY4pZo0UZJCFbEbomWqFN7zkAl-KwYQwosck4sME";
    mockMvc.perform(post(LOGOUT)
            .header("Authorization", "Bearer " + expiredToken)
        )
        .andExpect(status().isUnauthorized());

    assertFalse(invalidatedJwtRepository.existsByToken(expiredToken));
  }

  @Test
  void logout_whenNoToken_shouldRespondWithUnauthorized() throws Exception {
    mockMvc.perform(post(LOGOUT))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void register_whenValidInput_shouldRespondWithCreatedAndUserId() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());

    User user = userRepository.findByUsernameIgnoreCase("testUsername2").orElseThrow();

    assertEquals(input.getUsername(), user.getUsername());
    assertEquals(input.getEmail(), user.getEmail());
    assertEquals(input.getFirstName(), user.getFirstName());
    assertEquals(input.getLastName(), user.getLastName());
    assertEquals(input.getPhoneNo(), user.getPhoneNumber());
    assertTrue(passwordEncoder.matches(input.getPassword(), user.getPassword()));

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertFalse(codes.isEmpty());
    assertEquals(1, codes.size());
  }

  @Test
  void register_whenUsernameTooShort_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("l")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Username must be at least 2 characters in length")))
        .andExpect(jsonPath("$.errors[0].field", is("username")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenBlankUsername_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenNullUsername_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Username cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("username")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenUsernameTaken_shouldRespondWithConflictAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors[0].message", is("User with the username \"" + input.getUsername() + "\" already exists")))
        .andExpect(jsonPath("$.statusCode", is("CONFLICT")));

    Optional<User> userMaybe = userRepository.findByEmailIgnoreCase(input.getEmail());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenInvalidEmailFormat_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("not a valid email")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid email format")))
        .andExpect(jsonPath("$.errors[0].field", is("email")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenBlankEmailFormat_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenNullEmailFormat_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email(null)
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Email cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("email")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenEmailTaken_shouldRespondWithConflictAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors[0].message", is("User with email \"" + input.getEmail() + "\" already exists")))
        .andExpect(jsonPath("$.statusCode", is("CONFLICT")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenPasswordTooShort_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("pswrd")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Password must be at least 6 characters in length")))
        .andExpect(jsonPath("$.errors[0].field", is("password")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenBlankPassword_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenNullPassword_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password(null)
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Password cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("password")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenFirstNameTooLong_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("TestTestTestTestTestTestTestTestTestTestT")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("First name must be between 2 and 40 characters long")))
        .andExpect(jsonPath("$.errors[0].field", is("firstName")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenFirstNameTooShort_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("T")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("First name must be between 2 and 40 characters long")))
        .andExpect(jsonPath("$.errors[0].field", is("firstName")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenNullFirstName_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName(null)
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("First name cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("firstName")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenLastNameTooLong_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("TestovTestovTestovTestovTestovTestovTestov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Last name must be between 2 and 40 characters long")))
        .andExpect(jsonPath("$.errors[0].field", is("lastName")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenLastNameTooShort_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("T")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Last name must be between 2 and 40 characters long")))
        .andExpect(jsonPath("$.errors[0].field", is("lastName")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenNullLastName_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName(null)
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Last name cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("lastName")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenInvalidPhoneNoFormat_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("90451232115")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid phoneNo format")))
        .andExpect(jsonPath("$.errors[0].field", is("phoneNo")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void register_whenNullPhoneNo_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo(null)
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("PhoneNo cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("phoneNo")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }


  @Test
  void register_whenPhoneNoTaken_shouldRespondWithConflictAndErrorResult() throws Exception {
    RegisterInput input = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 123456789")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors[0].message", is("User with phoneNo \"" + input.getPhoneNo() + "\" already exists")))
        .andExpect(jsonPath("$.statusCode", is("CONFLICT")));

    Optional<User> userMaybe = userRepository.findByUsernameIgnoreCase(input.getUsername());
    assertTrue(userMaybe.isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void recoverPassword_whenCorrectEmail_shouldSendEmailAndRespondWithOk() throws Exception {
    RecoverPasswordInput input = RecoverPasswordInput.builder()
        .email("test.email@example.com")
        .build();

    mockMvc.perform(post(RECOVER_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isOk());

    List<RecoveryCode> codes = recoveryCodeRepository.findAll();
    assertFalse(codes.isEmpty());
    assertFalse(codes.getFirst().getHashedOtp().isEmpty());
    assertTrue(codes.getFirst().getCreatedAt().isBefore(Instant.now()));
  }

  @Test
  void recoverPassword_whenInvalidEmail_shouldSendEmailAndRespondWithOk() throws Exception {
    RecoverPasswordInput input = RecoverPasswordInput.builder()
        .email("not a valid email")
        .build();

    mockMvc.perform(post(RECOVER_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isOk());

    List<RecoveryCode> codes = recoveryCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void recoverPassword_whenNullEmail_shouldSendEmailAndRespondWithOk() throws Exception {
    RecoverPasswordInput input = RecoverPasswordInput.builder()
        .email(null)
        .build();

    mockMvc.perform(post(RECOVER_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isOk());

    List<RecoveryCode> codes = recoveryCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void changePasswordUsingRecoveryCode_whenValidInput_shouldChangePasswordAndRespondWithOk() throws Exception {
    UUID id = userRepository.findByUsernameIgnoreCase("testUsername")
        .orElseThrow().getId();
    RecoveryCode code = RecoveryCode.builder()
        .hashedOtp(passwordEncoder.encode("randomOTP"))
        .userId(id)
        .createdAt(Instant.now())
        .build();
    recoveryCodeRepository.save(code);

    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("randomOTP")
        .email("test.email@example.com")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

    User updatedUser = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertTrue(passwordEncoder.matches(input.getNewPassword(), updatedUser.getPassword()));

    List<RecoveryCode> codes = recoveryCodeRepository.findAll();
    assertTrue(codes.isEmpty());
  }

  @Test
  void changePasswordUsingRecoveryCode_whenWrongRecoveryCode_shouldRespondWithBadRequest() throws Exception {
    UUID id = userRepository.findByUsernameIgnoreCase("testUsername")
        .orElseThrow().getId();
    RecoveryCode code = RecoveryCode.builder()
        .hashedOtp(passwordEncoder.encode("randomOTP"))
        .userId(id)
        .createdAt(Instant.now())
        .build();
    recoveryCodeRepository.save(code);

    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("nonexistantcode")
        .email("test.email@example.com")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid recovery code")))
        .andExpect(jsonPath("$.statusCode", is("BAD_REQUEST")));

    User updatedUser = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), updatedUser.getPassword()));

    List<RecoveryCode> codes = recoveryCodeRepository.findAll();
    assertFalse(codes.isEmpty());
    assertEquals(1, codes.size());
  }

  @Test
  void changePasswordUsingRecoveryCode_whenWrongEmail_shouldRespondWithBadRequest() throws Exception {
    UUID id = userRepository.findByUsernameIgnoreCase("testUsername")
        .orElseThrow().getId();
    RecoveryCode code = RecoveryCode.builder()
        .hashedOtp(passwordEncoder.encode("randomOTP"))
        .userId(id)
        .createdAt(Instant.now())
        .build();
    recoveryCodeRepository.save(code);

    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("randomOTP")
        .email("nonexistantemail@example.com")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message", is("No recovery request has been made for this email")))
        .andExpect(jsonPath("$.statusCode", is("BAD_REQUEST")));

    User updatedUser = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), updatedUser.getPassword()));

    List<RecoveryCode> codes = recoveryCodeRepository.findAll();
    assertFalse(codes.isEmpty());
    assertEquals(1, codes.size());
  }

  @Test
  void changePasswordUsingRecoveryCode_whenInvalidEmailFormat_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("randomOTP")
        .email("not a valid email format")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid email format")))
        .andExpect(jsonPath("$.errors[0].field", is("email")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void changePasswordUsingRecoveryCode_whenNullEmail_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("randomOTP")
        .email(null)
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Email cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("email")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void changePasswordUsingRecoveryCode_whenBlankEmail_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("randomOTP")
        .email("")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void changePasswordUsingRecoveryCode_whenBlankCode_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("")
        .email("test.email@example.com")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Recovery code cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("recoveryCode")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void changePasswordUsingRecoveryCode_whenNullCode_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode(null)
        .email("test.email@example.com")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Recovery code cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("recoveryCode")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void changePasswordUsingRecoveryCode_whenPasswordTooShort_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("randomOTP")
        .email("test.email@example.com")
        .newPassword("pswrd")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("New password must be at least 6 characters in length")))
        .andExpect(jsonPath("$.errors[0].field", is("newPassword")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void changePasswordUsingRecoveryCode_whenBlankPassword_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("randomOTP")
        .email("test.email@example.com")
        .newPassword("")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void changePasswordUsingRecoveryCode_whenNullPassword_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ChangePasswordUsingRecoveryCodeInput input = ChangePasswordUsingRecoveryCodeInput.builder()
        .recoveryCode("randomOTP")
        .email("test.email@example.com")
        .newPassword(null)
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD_USING_RECOVERY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("New password cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("newPassword")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void confirmRegistration_whenValidCode_shouldVerifyUserAndRespondWithOk() throws Exception {
    RegisterInput registerInput = RegisterInput.builder()
        .username("testUsername5")
        .password("testpassword")
        .email("test.email5@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 981654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerInput))
        )
        .andExpect(status().isCreated());

    VerificationCode code = verificationCodeRepository.findAll().getFirst();
    ConfirmRegistrationInput input = ConfirmRegistrationInput.builder()
        .confirmationCode(code.getCode())
        .build();

    mockMvc.perform(post(CONFIRM_REGISTRATION)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertTrue(codes.isEmpty());

    User user = userRepository.findByUsernameIgnoreCase("testUsername5").orElseThrow();
    assertEquals(true, user.getIsVerified());
  }

  @Test
  void confirmRegistration_whenWrongCode_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    RegisterInput registerInput = RegisterInput.builder()
        .username("testUsername2")
        .password("testpassword")
        .email("test.email2@example.com")
        .firstName("Test")
        .lastName("Testov")
        .phoneNo("+359 987654321")
        .build();

    mockMvc.perform(post(REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerInput))
        )
        .andExpect(status().isCreated());

    ConfirmRegistrationInput input = ConfirmRegistrationInput.builder()
        .confirmationCode("wrongCode")
        .build();

    mockMvc.perform(post(CONFIRM_REGISTRATION)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid verification code")))
        .andExpect(jsonPath("$.statusCode", is("BAD_REQUEST")));

    List<VerificationCode> codes = verificationCodeRepository.findAll();
    assertFalse(codes.isEmpty());

    User user = userRepository.findByUsernameIgnoreCase("testUsername2").orElseThrow();
    assertEquals(false, user.getIsVerified());
  }

  @Test
  void confirmRegistration_whenBlankCode_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ConfirmRegistrationInput input = ConfirmRegistrationInput.builder()
        .confirmationCode("")
        .build();

    mockMvc.perform(post(CONFIRM_REGISTRATION)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Confirmation code cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("confirmationCode")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void confirmRegistration_whenNullCode_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    ConfirmRegistrationInput input = ConfirmRegistrationInput.builder()
        .confirmationCode(null)
        .build();

    mockMvc.perform(post(CONFIRM_REGISTRATION)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Confirmation code cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("confirmationCode")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void changePassword_whenValidInput_shouldChangePasswordAndRespondWithOk() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email("test.email@example.com")
        .oldPassword("testpassword")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
    User user = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertTrue(passwordEncoder.matches(input.getNewPassword(), user.getPassword()));
  }

  @Test
  void changePassword_whenMismatchEmail_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email("nonexistant.email@example.com")
        .oldPassword("testpassword")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid credentials")))
        .andExpect(jsonPath("$.statusCode", is("BAD_REQUEST")));

    User user = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), user.getPassword()));
  }

  @Test
  void changePassword_whenBlankEmail_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email("")
        .oldPassword("testpassword")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    User user = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), user.getPassword()));
  }

  @Test
  void changePassword_whenNullEmail_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email(null)
        .oldPassword("testpassword")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Email cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("email")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    User user = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), user.getPassword()));
  }

  @Test
  void changePassword_whenBlankOldPassword_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email("test.email@example.com")
        .oldPassword("")
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Old password cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("oldPassword")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    User user = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), user.getPassword()));
  }

  @Test
  void changePassword_whenNullOldPassword_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email("test.email@example.com")
        .oldPassword(null)
        .newPassword("newpassword")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("Old password cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("oldPassword")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    User user = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), user.getPassword()));
  }

  @Test
  void changePassword_whenBlankNewPassword_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email("test.email@example.com")
        .oldPassword("testpassword")
        .newPassword("")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    User user = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), user.getPassword()));
  }

  @Test
  void changePassword_whenNullNewPassword_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email("test.email@example.com")
        .oldPassword("testpassword")
        .newPassword(null)
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("New password cannot be blank")))
        .andExpect(jsonPath("$.errors[0].field", is("newPassword")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));
  }

  @Test
  void changePassword_whenNewPasswordTooShort_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email("test.email@example.com")
        .oldPassword("testpassword")
        .newPassword("pswrd")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("New password has to be at least 6 characters in length")))
        .andExpect(jsonPath("$.errors[0].field", is("newPassword")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    User user = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), user.getPassword()));
  }

  @Test
  void changePassword_whenInvalidToken_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = "notavalidtoken";

    ChangePasswordInput input = ChangePasswordInput.builder()
        .email("test.email@example.com")
        .oldPassword("testpassword")
        .newPassword("pswrd")
        .build();

    mockMvc.perform(post(CHANGE_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid JWT format")))
        .andExpect(jsonPath("$.statusCode", is("UNAUTHORIZED")));

    User user = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();
    assertFalse(passwordEncoder.matches(input.getNewPassword(), user.getPassword()));
  }

  @Test
  void promote_whenCorrectTokenAndUserId_shouldPromoteUserToAdmin() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    User userToPromote = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();

    PromoteInput input = PromoteInput.builder()
        .userId(userToPromote.getId().toString())
        .build();

    mockMvc.perform(post(PROMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

    User promotedUser = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();
    assertTrue(promotedUser.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void promote_whenSelfPromote_shouldRespondWithConflictAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    User userToPromote = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();

    PromoteInput input = PromoteInput.builder()
        .userId(userToPromote.getId().toString())
        .build();

    mockMvc.perform(post(PROMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors[0].message", is("Cannot promote yourself")))
        .andExpect(jsonPath("$.statusCode", is("CONFLICT")));

    User promotedUser = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();
    assertFalse(promotedUser.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void promote_whenInvalidToken_shouldRespondWithUnauthorized() throws Exception {
    String token = "tknoenekn";

    User userToPromote = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();

    PromoteInput input = PromoteInput.builder()
        .userId(userToPromote.getId().toString())
        .build();

    mockMvc.perform(post(PROMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid JWT format")))
        .andExpect(jsonPath("$.statusCode", is("UNAUTHORIZED")));

    User promotedUser = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();
    assertFalse(promotedUser.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void promote_whenNoAdminRoles_shouldRespondWithForbidden() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    User userToPromote = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();

    PromoteInput input = PromoteInput.builder()
        .userId(userToPromote.getId().toString())
        .build();

    mockMvc.perform(post(PROMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errors[0].message", is("Only admins can access this resource")))
        .andExpect(jsonPath("$.statusCode", is("FORBIDDEN")));

    User promotedUser = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();
    assertFalse(promotedUser.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void promote_invalidUserId_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    PromoteInput input = PromoteInput.builder()
        .userId("invalidId")
        .build();

    mockMvc.perform(post(PROMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("User id must be a valid UUID string")))
        .andExpect(jsonPath("$.errors[0].field", is("userId")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    User promotedUser = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();
    assertFalse(promotedUser.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void promote_wrongUserId_shouldRespondWithBadeRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    PromoteInput input = PromoteInput.builder()
        .userId("5c48571a-a036-4557-bee0-118ddb4164d5")
        .build();

    mockMvc.perform(post(PROMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message", is("Entity of type User with id=" + input.getUserId() + " not found")))
        .andExpect(jsonPath("$.statusCode", is("BAD_REQUEST")));

    User promotedUser = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();
    assertFalse(promotedUser.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void demote_whenCorrectTokenAndUserId_shouldPromoteUserToAdmin() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    User userToDemote = userRepository.findByUsernameIgnoreCase("admin123").orElseThrow();

    DemoteInput input = DemoteInput.builder()
        .userId(userToDemote.getId().toString())
        .build();

    mockMvc.perform(post(DEMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

    User demotedUser = userRepository.findByUsernameIgnoreCase("admin123").orElseThrow();
    assertFalse(demotedUser.getRoles().stream().anyMatch(r -> r.getType().equals(RoleEnum.ADMIN)));
  }

  @Test
  void demote_whenSelfDemote_shouldRespondWithConflictAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    User userToDemote = userRepository.findByUsernameIgnoreCase("testUsername").orElseThrow();

    DemoteInput input = DemoteInput.builder()
        .userId(userToDemote.getId().toString())
        .build();

    mockMvc.perform(post(DEMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors[0].message", is("Cannot demote yourself")))
        .andExpect(jsonPath("$.statusCode", is("CONFLICT")));

    User user = userRepository.findByUsernameIgnoreCase("admin123").orElseThrow();
    assertTrue(user.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void demote_whenInvalidToken_shouldRespondWithUnauthorized() throws Exception {
    String token = "tknoenekn";

    User userToDemote = userRepository.findByUsernameIgnoreCase("user123").orElseThrow();

    DemoteInput input = DemoteInput.builder()
        .userId(userToDemote.getId().toString())
        .build();

    mockMvc.perform(post(DEMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].message", is("Invalid JWT format")))
        .andExpect(jsonPath("$.statusCode", is("UNAUTHORIZED")));

    User user = userRepository.findByUsernameIgnoreCase("admin123").orElseThrow();
    assertTrue(user.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void demote_whenNoAdminRoles_shouldRespondWithForbidden() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    User userToDemote = userRepository.findByUsernameIgnoreCase("admin123").orElseThrow();

    DemoteInput input = DemoteInput.builder()
        .userId(userToDemote.getId().toString())
        .build();

    mockMvc.perform(post(DEMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errors[0].message", is("Only admins can access this resource")))
        .andExpect(jsonPath("$.statusCode", is("FORBIDDEN")));

    User user = userRepository.findByUsernameIgnoreCase("admin123").orElseThrow();
    assertTrue(user.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void demote_whenInvalidUserId_shouldRespondWithUnprocessableEntityAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    DemoteInput input = DemoteInput.builder()
        .userId("invalidId")
        .build();

    mockMvc.perform(post(DEMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0].message", is("User id must be a valid UUID string")))
        .andExpect(jsonPath("$.errors[0].field", is("userId")))
        .andExpect(jsonPath("$.statusCode", is("UNPROCESSABLE_ENTITY")));

    User user = userRepository.findByUsernameIgnoreCase("admin123").orElseThrow();
    assertTrue(user.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }

  @Test
  void demote_whenWrongUserId_shouldRespondWithBadRequestAndErrorResult() throws Exception {
    String token = jwtProvider.createToken("testUsername",
        List.of(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN,
            com.tinqinacademy.authentication.api.enums.RoleEnum.USER));

    DemoteInput input = DemoteInput.builder()
        .userId("5c48571a-a036-4557-bee0-118ddb4164d5")
        .build();

    mockMvc.perform(post(DEMOTE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(input))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message", is("Entity of type User with id=" + input.getUserId() + " not found")))
        .andExpect(jsonPath("$.statusCode", is("BAD_REQUEST")));

    User user = userRepository.findByUsernameIgnoreCase("admin123").orElseThrow();
    assertTrue(user.getRoles().stream().map(Role::getType).toList().contains(RoleEnum.ADMIN));
  }
}