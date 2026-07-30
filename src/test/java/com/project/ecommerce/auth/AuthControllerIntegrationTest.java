package com.project.ecommerce.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ecommerce.Enums.USER_ROLE;
import com.project.ecommerce.Model.RefreshToken;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Model.VerificationCode;
import com.project.ecommerce.Repository.CartRepository;
import com.project.ecommerce.Repository.RefreshTokenRepository;
import com.project.ecommerce.Repository.UserRepository;
import com.project.ecommerce.Repository.VerifcationCodeRepository;
import com.project.ecommerce.Service.EmailService;
import jakarta.transaction.Transactional;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.startsWith;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerifcationCodeRepository verificationCodeRepository;

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setup(){
        verificationCodeRepository.deleteAll();
        userRepository.deleteAll();


        User user = new User();

        user.setEmail(
                "test@gmail.com"
        );

        user.setFullName(
                "Test User"
        );


        user.setPassword(
                passwordEncoder.encode("123456")
        );


        user.setRole(
                USER_ROLE.ROLE_CUSTOMER
        );


        userRepository.save(user);
    }


    @Test
    void login_success_should_return_token()
            throws Exception {


        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                {
                    "email":"test@gmail.com",
                    "password":"123456"
                }
                """)
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.jwt")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.refreshToken")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Login success")
                )

                .andExpect(
                        jsonPath("$.role")
                                .value("ROLE_CUSTOMER")
                );
    }
    @Test
    void login_wrong_password_should_return_401()
            throws Exception {


        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
            {
                "email":"test@gmail.com",
                "password":"wrong-password"
            }
            """)
                )


                .andExpect(
                        status().isUnauthorized()
                )


                .andExpect(
                        jsonPath("$.message")
                                .value("Invalid email or password")
                );
    }
    @Test
    void signup_success_should_create_new_user() throws Exception {

        VerificationCode verificationCode = new VerificationCode();

        verificationCode.setEmail("newuser@gmail.com");
        verificationCode.setOtp("123456");
        verificationCode.setCreatedAt(LocalDateTime.now());

        verificationCodeRepository.save(verificationCode);

        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"newuser@gmail.com",
                        "fullName":"New User",
                        "otp":"123456",
                        "password":"12345678"
                    }
                    """)
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.jwt").exists())

                .andExpect(jsonPath("$.refreshToken").exists())

                .andExpect(jsonPath("$.message")
                        .value("Register success"))

                .andExpect(jsonPath("$.role")
                        .value("ROLE_CUSTOMER"));

        User createdUser =
                userRepository.findByEmail("newuser@gmail.com");

        assertNotNull(createdUser);

        assertEquals(
                "New User",
                createdUser.getFullName()
        );

        assertEquals(
                USER_ROLE.ROLE_CUSTOMER,
                createdUser.getRole()
        );

        assertTrue(
                passwordEncoder.matches(
                        "12345678",
                        createdUser.getPassword()
                )
        );

        assertNull(
                verificationCodeRepository.findByEmail(
                        "newuser@gmail.com"
                )
        );
    }

    @Test
    void send_signup_otp_email_already_exists() throws Exception {

        mockMvc.perform(
                        post("/auth/signup/send-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"test@gmail.com"
                    }
                    """)
                )

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.status")
                        .value(400))

                .andExpect(jsonPath("$.message")
                        .value("Email already exists"))

                .andExpect(jsonPath("$.path")
                        .value("/auth/signup/send-otp"));

        assertNull(
                verificationCodeRepository.findByEmail("test@gmail.com")
        );

        verify(emailService, never())
                .senVerificationOtpEmail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }
    @Test
    void send_signup_otp_success() throws Exception {

        String email = "newuser@gmail.com";

        mockMvc.perform(
                        post("/auth/signup/send-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"newuser@gmail.com"
                    }
                    """)
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.message")
                        .value("OTP sent successfully"));

        VerificationCode verificationCode =
                verificationCodeRepository.findByEmail(email);

        assertNotNull(verificationCode);

        assertEquals(email, verificationCode.getEmail());

        assertNotNull(verificationCode.getOtp());

        assertNotNull(verificationCode.getCreatedAt());

        verify(emailService).senVerificationOtpEmail(
                eq(email),
                anyString(),
                eq("Signup Verification OTP"),
                org.mockito.ArgumentMatchers.startsWith(
                        "Your signup verification OTP is: "
                )
        );
    }
    @Test
    void send_signup_otp_should_return_429_when_requesting_again_within_60_seconds()
            throws Exception {

        VerificationCode verificationCode = new VerificationCode();

        verificationCode.setEmail("newuser@gmail.com");
        verificationCode.setOtp("123456");
        verificationCode.setCreatedAt(LocalDateTime.now());

        verificationCodeRepository.save(verificationCode);

        mockMvc.perform(
                        post("/auth/signup/send-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"newuser@gmail.com"
                    }
                    """)
                )

                .andExpect(status().isTooManyRequests())

                .andExpect(jsonPath("$.message")
                        .value(startsWith("Please wait")));
    }
    @Test
    void signup_should_fail_when_otp_is_invalid() throws Exception {

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail("newuser@gmail.com");
        verificationCode.setOtp("123456");
        verificationCode.setCreatedAt(LocalDateTime.now());

        verificationCodeRepository.save(verificationCode);

        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"newuser@gmail.com",
                        "fullName":"New User",
                        "password":"123456",
                        "otp":"999999"
                    }
                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Wrong OTP"));

        assertNull(
                userRepository.findByEmail("newuser@gmail.com")
        );

        VerificationCode otp =
                verificationCodeRepository.findByEmail("newuser@gmail.com");

        assertNotNull(otp);
        assertEquals("123456", otp.getOtp());
    }

    @Test
    void signup_should_fail_when_otp_is_expired()
            throws Exception {


        String email = "expired@gmail.com";


        VerificationCode verificationCode =
                new VerificationCode();

        verificationCode.setEmail(email);

        verificationCode.setOtp("123456");

        verificationCode.setCreatedAt(
                LocalDateTime.now()
                        .minusMinutes(4)
        );

        verificationCodeRepository.save(verificationCode);



        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"expired@gmail.com",
                        "fullName":"Expired User",
                        "otp":"123456",
                        "password":"123456"
                    }
                    """)
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.error")
                                .value("OTP_EXPIRED")
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("OTP expired")
                );


        // User không được tạo
        assertNull(
                userRepository.findByEmail(email)
        );


        // OTP hết hạn bị xóa theo logic validateOtp()
        assertNull(
                verificationCodeRepository.findByEmail(email)
        );
    }

    @Test
    void signup_should_fail_when_email_already_exists()
            throws Exception {


        String email = "existing@gmail.com";


        // Tạo user đã tồn tại
        User existingUser = new User();

        existingUser.setEmail(email);

        existingUser.setFullName(
                "Existing User"
        );

        existingUser.setPassword(
                passwordEncoder.encode("123456")
        );

        existingUser.setRole(
                USER_ROLE.ROLE_CUSTOMER
        );


        userRepository.save(existingUser);



        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"existing@gmail.com",
                        "fullName":"New User",
                        "otp":"123456",
                        "password":"123456"
                    }
                    """)
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Email already exists")
                );


        // Đảm bảo user cũ vẫn còn
        User user =
                userRepository.findByEmail(email);

        assertNotNull(user);

        assertEquals(
                "Existing User",
                user.getFullName()
        );

        assertEquals(
                "Existing User",
                userRepository.findByEmail(email)
                        .getFullName()
        );
    }

    @Test
    void signup_should_fail_when_otp_is_wrong()
            throws Exception {


        String email = "wrongotp@gmail.com";


        VerificationCode verificationCode =
                new VerificationCode();

        verificationCode.setEmail(email);

        verificationCode.setOtp("123456");

        verificationCode.setCreatedAt(
                LocalDateTime.now()
        );


        verificationCodeRepository.save(
                verificationCode
        );



        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"wrongotp@gmail.com",
                        "fullName":"Wrong OTP User",
                        "otp":"999999",
                        "password":"123456"
                    }
                    """)
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Wrong OTP")
                )

                .andExpect(
                        jsonPath("$.error")
                                .value("INVALID_OTP")
                );



        // User không được tạo
        assertNull(
                userRepository.findByEmail(email)
        );


        // OTP sai không bị xóa
        VerificationCode savedOtp =
                verificationCodeRepository.findByEmail(email);


        assertNotNull(savedOtp);

        assertEquals(
                "123456",
                savedOtp.getOtp()
        );
    }
    @Test
    void signup_success_should_create_cart()
            throws Exception {


        String email = "newcustomer@gmail.com";


        VerificationCode verificationCode =
                new VerificationCode();

        verificationCode.setEmail(email);

        verificationCode.setOtp("123456");

        verificationCode.setCreatedAt(
                LocalDateTime.now()
        );


        verificationCodeRepository.save(
                verificationCode
        );



        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"newcustomer@gmail.com",
                        "fullName":"New Customer",
                        "otp":"123456",
                        "password":"123456"
                    }
                    """)
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.jwt")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.refreshToken")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Register success")
                )

                .andExpect(
                        jsonPath("$.role")
                                .value("ROLE_CUSTOMER")
                );



        // Check User được tạo
        User savedUser =
                userRepository.findByEmail(email);


        assertNotNull(savedUser);


        assertEquals(
                "New Customer",
                savedUser.getFullName()
        );


        assertEquals(
                USER_ROLE.ROLE_CUSTOMER,
                savedUser.getRole()
        );



        // Check Cart được tạo
        assertNotNull(
                savedUser.getId()
        );


        assertNotNull(
                cartRepository.findByUser_Id(
                        savedUser.getId()
                )
        );



        // OTP phải bị xóa sau khi signup thành công
        VerificationCode deletedOtp =
                verificationCodeRepository.findByEmail(email);


        assertNull(deletedOtp);
    }

    @Test
    void forgot_password_send_otp_success()
            throws Exception {


        String email = "forgot@gmail.com";


        User user = new User();

        user.setEmail(email);

        user.setFullName(
                "Forgot User"
        );

        user.setPassword(
                passwordEncoder.encode("123456")
        );

        user.setRole(
                USER_ROLE.ROLE_CUSTOMER
        );


        userRepository.save(user);



        mockMvc.perform(
                        post("/auth/forgot-password/send-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"forgot@gmail.com"
                    }
                    """)
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("OTP sent successfully")
                );



        VerificationCode otp =
                verificationCodeRepository.findByEmail(email);


        assertNotNull(otp);


        assertEquals(
                email,
                otp.getEmail()
        );


        assertNotNull(
                otp.getOtp()
        );


        assertNotNull(
                otp.getCreatedAt()
        );



        verify(emailService)
                .senVerificationOtpEmail(
                        eq(email),
                        anyString(),
                        eq("Reset Password OTP"),
                        org.mockito.ArgumentMatchers.startsWith(
                                "Your password reset OTP is: "
                        )
                );
    }

    @Test
    void forgot_password_send_otp_should_fail_when_email_not_found()
            throws Exception {


        String email = "unknown@gmail.com";


        mockMvc.perform(
                        post("/auth/forgot-password/send-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"unknown@gmail.com"
                    }
                    """)
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Email not found")
                );


        // Không có OTP được tạo

        VerificationCode otp =
                verificationCodeRepository.findByEmail(email);

        assertNull(otp);



        // Không gửi email

        verify(emailService, never())
                .senVerificationOtpEmail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void reset_password_success()
            throws Exception {


        String email = "test@gmail.com";
        String otpValue = "123456";


        VerificationCode verificationCode =
                new VerificationCode();

        verificationCode.setEmail(email);
        verificationCode.setOtp(otpValue);
        verificationCode.setCreatedAt(
                LocalDateTime.now()
        );


        verificationCodeRepository.save(
                verificationCode
        );


        mockMvc.perform(
                        post("/auth/forgot-password/reset")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"test@gmail.com",
                        "otp":"123456",
                        "newPassword":"newPassword123"
                    }
                    """)
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Password reset successfully")
                );



        // kiểm tra password đã đổi

        User updatedUser =
                userRepository.findByEmail(email);


        assertNotNull(updatedUser);


        assertTrue(
                passwordEncoder.matches(
                        "newPassword123",
                        updatedUser.getPassword()
                )
        );
        // OTP đã sử dụng phải bị xóa

        VerificationCode deletedOtp =
                verificationCodeRepository.findByEmail(email);


        assertNull(deletedOtp);
    }

    @Test
    void reset_password_should_fail_when_otp_wrong()
            throws Exception {


        String email = "forgot@gmail.com";


        User user = new User();

        user.setEmail(email);

        user.setFullName("Forgot User");

        user.setPassword(
                passwordEncoder.encode("old-password")
        );

        user.setRole(
                USER_ROLE.ROLE_CUSTOMER
        );

        userRepository.save(user);



        VerificationCode verificationCode =
                new VerificationCode();

        verificationCode.setEmail(email);

        verificationCode.setOtp("123456");

        verificationCode.setCreatedAt(
                LocalDateTime.now()
        );

        verificationCodeRepository.save(
                verificationCode
        );



        mockMvc.perform(
                        post("/auth/forgot-password/reset")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"forgot@gmail.com",
                        "otp":"999999",
                        "newPassword":"new-password"
                    }
                    """)
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Wrong OTP")
                );



        User savedUser =
                userRepository.findByEmail(email);


        assertNotNull(savedUser);


        assertFalse(
                passwordEncoder.matches(
                        "new-password",
                        savedUser.getPassword()
                )
        );


        VerificationCode otpAfterFail =
                verificationCodeRepository.findByEmail(email);


        assertNotNull(otpAfterFail);
    }

    @Test
    void reset_password_should_fail_when_otp_expired()
            throws Exception {


        String email = "expired-reset@gmail.com";


        User user = new User();

        user.setEmail(email);

        user.setFullName(
                "Expired Reset User"
        );

        user.setPassword(
                passwordEncoder.encode("old-password")
        );

        user.setRole(
                USER_ROLE.ROLE_CUSTOMER
        );


        userRepository.save(user);



        VerificationCode verificationCode =
                new VerificationCode();


        verificationCode.setEmail(email);


        verificationCode.setOtp("123456");


        // OTP hết hạn > 3 phút

        verificationCode.setCreatedAt(
                LocalDateTime.now()
                        .minusMinutes(5)
        );


        verificationCodeRepository.save(
                verificationCode
        );



        mockMvc.perform(
                        post("/auth/forgot-password/reset")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"expired-reset@gmail.com",
                        "otp":"123456",
                        "newPassword":"new-password"
                    }
                    """)
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("OTP expired")
                );



        // Password không được thay đổi

        User savedUser =
                userRepository.findByEmail(email);


        assertNotNull(savedUser);


        assertTrue(
                passwordEncoder.matches(
                        "old-password",
                        savedUser.getPassword()
                )
        );



        // OTP expired bị delete trong validateOtp()

        VerificationCode deletedOtp =
                verificationCodeRepository.findByEmail(email);


        assertNull(deletedOtp);
    }

    @Test
    void refresh_token_success()
            throws Exception {


        String email = "refresh@gmail.com";


        User user = new User();

        user.setEmail(email);

        user.setFullName(
                "Refresh User"
        );

        user.setPassword(
                passwordEncoder.encode("123456")
        );

        user.setRole(
                USER_ROLE.ROLE_CUSTOMER
        );


        user = userRepository.save(user);



        RefreshToken refreshToken =
                new RefreshToken();


        refreshToken.setToken(
                "refresh-token-test-123"
        );


        refreshToken.setCreatedAt(
                LocalDateTime.now()
        );


        refreshToken.setExpiryDate(
                LocalDateTime.now()
                        .plusDays(30)
        );


        refreshToken.setUser(user);



        refreshTokenRepository.save(refreshToken);



        mockMvc.perform(
                        post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "refreshToken":"refresh-token-test-123"
                    }
                    """)
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.jwt")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.refreshToken")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.role")
                                .value("ROLE_CUSTOMER")
                );
    }

    @Test
    void refresh_token_should_fail_when_token_expired()
            throws Exception {


        User user = userRepository.findByEmail(
                "test@gmail.com"
        );


        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(
                "expired-refresh-token"
        );

        refreshToken.setCreatedAt(
                LocalDateTime.now().minusDays(31)
        );

        refreshToken.setExpiryDate(
                LocalDateTime.now().minusDays(1)
        );

        refreshToken.setUser(user);


        refreshTokenRepository.save(refreshToken);



        mockMvc.perform(
                        post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "refreshToken":"expired-refresh-token"
                    }
                    """)
                )


                .andExpect(
                        status().isBadRequest()
                )


                .andExpect(
                        jsonPath("$.message")
                                .value("Refresh token expired")
                );


        assertFalse(
                refreshTokenRepository
                        .findByToken("expired-refresh-token")
                        .isPresent()
        );
    }

    @Test
    void refresh_token_should_fail_when_token_not_found()
            throws Exception {


        mockMvc.perform(
                        post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "refreshToken":"invalid-refresh-token"
                    }
                    """)
                )


                .andExpect(
                        status().isBadRequest()
                )


                .andExpect(
                        jsonPath("$.message")
                                .value("Refresh token not found")
                );


        assertFalse(
                refreshTokenRepository
                        .findByToken("invalid-refresh-token")
                        .isPresent()
        );
    }

    @Test
    void logout_success_should_delete_refresh_token()
            throws Exception {


        User user = userRepository.findByEmail(
                "test@gmail.com"
        );


        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(
                "logout-test-token"
        );

        refreshToken.setCreatedAt(
                LocalDateTime.now()
        );

        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(30)
        );

        refreshToken.setUser(user);


        refreshTokenRepository.save(refreshToken);



        mockMvc.perform(
                        post("/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "refreshToken":"logout-test-token"
                    }
                    """)
                )


                .andExpect(
                        status().isOk()
                )


                .andExpect(
                        jsonPath("$.message")
                                .value("Logout success")
                );


        assertFalse(
                refreshTokenRepository
                        .findByToken("logout-test-token")
                        .isPresent()
        );
    }

    @Test
    void logout_should_fail_when_token_not_found()
            throws Exception {


        mockMvc.perform(
                        post("/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "refreshToken":"invalid-token"
                    }
                    """)
                )


                .andExpect(
                        status().isBadRequest()
                )


                .andExpect(
                        jsonPath("$.message")
                                .value("Refresh token not found")
                );
    }

    @Test
    void login_should_fail_when_email_not_found()
            throws Exception {


        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "email":"notfound@gmail.com",
                        "password":"123456"
                    }
                    """)
                )


                .andExpect(
                        status().isUnauthorized()
                )


                .andExpect(
                        jsonPath("$.message")
                                .value("Invalid email or password")
                );
    }

    @Test
    void refresh_token_should_rotate_successfully()
            throws Exception {


        User user = userRepository.findByEmail(
                "test@gmail.com"
        );


        RefreshToken oldToken = new RefreshToken();

        oldToken.setToken(
                "old-refresh-token"
        );

        oldToken.setCreatedAt(
                LocalDateTime.now()
        );

        oldToken.setExpiryDate(
                LocalDateTime.now()
                        .plusDays(30)
        );

        oldToken.setUser(user);


        refreshTokenRepository.save(oldToken);



        MvcResult result =
                mockMvc.perform(
                                post("/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                            {
                                "refreshToken":"old-refresh-token"
                            }
                            """)
                        )


                        .andExpect(
                                status().isOk()
                        )


                        .andExpect(
                                jsonPath("$.jwt")
                                        .exists()
                        )


                        .andExpect(
                                jsonPath("$.refreshToken")
                                        .exists()
                        )


                        .andExpect(
                                jsonPath("$.message")
                                        .value("Refresh token success")
                        )


                        .andReturn();



        String response =
                result.getResponse()
                        .getContentAsString();


        ObjectMapper mapper = new ObjectMapper();


        JsonNode json =
                mapper.readTree(response);


        String newRefreshToken =
                json.get("refreshToken")
                        .asText();



        // Token mới phải khác token cũ
        assertNotEquals(
                "old-refresh-token",
                newRefreshToken
        );



        // Token cũ đã bị revoke/xóa
        assertFalse(
                refreshTokenRepository
                        .findByToken(
                                "old-refresh-token"
                        )
                        .isPresent()
        );



        // Token mới tồn tại
        assertTrue(
                refreshTokenRepository
                        .findByToken(
                                newRefreshToken
                        )
                        .isPresent()
        );
    }
}