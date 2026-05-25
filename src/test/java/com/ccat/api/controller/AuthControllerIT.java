package com.ccat.api.controller;

import com.ccat.api.dto.request.UserLoginRequest;
import com.ccat.api.dto.request.UserRegisterRequest;
import com.ccat.api.util.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Uses in-memory H2 and MockMvc. No real email is sent.
 *
 * Covered endpoints:
 *   POST /api/v1/auth/register
 *   POST /api/v1/auth/login
 *   GET  /api/v1/auth/me
 *   POST /api/v1/auth/logout
 */
@DisplayName("AuthController - Integration Tests")
class AuthControllerIT extends IntegrationTestBase {

    private static final String BASE = "/api/v1/auth";

    // =========================================================================
    // POST /register
    // =========================================================================

    @Nested
    @DisplayName("POST /register - Registration")
    class RegisterTests {

        @Test
        @DisplayName("201 - Successful registration with valid data")
        void register_validRequest_returns201() throws Exception {
            var request = new UserRegisterRequest(
                    "bob@test.com", "Str0ng!Pass", "Bob", "Martin", "fr");

            mockMvc.perform(post(BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("409 - Email already used")
        void register_duplicateEmail_returns409() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            var request = new UserRegisterRequest(
                    "alice@test.com", "Str0ng!Pass", "Alice", "Dupont", "en");

            mockMvc.perform(post(BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("400 - Invalid email")
        void register_invalidEmail_returns400() throws Exception {
            var request = new UserRegisterRequest(
                    "not-an-email", "Str0ng!Pass", "Alice", "Dupont", "en");

            mockMvc.perform(post(BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 - Password too short (< 8 characters)")
        void register_shortPassword_returns400() throws Exception {
            var request = new UserRegisterRequest(
                    "charlie@test.com", "abc", "Charlie", "Brown", "en");

            mockMvc.perform(post(BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // POST /login
    // =========================================================================

    @Nested
    @DisplayName("POST /login - Login")
    class LoginTests {

        @Test
        @DisplayName("200 - Successful login with valid email/password")
        void login_validCredentials_returns200WithTokens() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            var request = new UserLoginRequest("alice@test.com", "Str0ng!Pass");

            mockMvc.perform(post(BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("401 - Wrong password")
        void login_wrongPassword_returns401() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            var request = new UserLoginRequest("alice@test.com", "WrongPass!");

            mockMvc.perform(post(BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 - Unknown email")
        void login_unknownEmail_returns401() throws Exception {
            var request = new UserLoginRequest("nobody@test.com", "Str0ng!Pass");

            mockMvc.perform(post(BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /me
    // =========================================================================

    @Nested
    @DisplayName("GET /me - Authenticated user profile")
    class MeTests {

        @Test
        @DisplayName("200 - Returns the authenticated user's profile")
        void me_authenticated_returnsUserProfile() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            mockMvc.perform(get(BASE + "/me")
                            .header("Authorization", bearerToken("alice@test.com")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.strEmail").value("alice@test.com"));
        }

        @Test
        @DisplayName("403 - No token provided (Spring Security 6 returns 403 without AuthenticationEntryPoint)")
        void me_noToken_returns403() throws Exception {
            mockMvc.perform(get(BASE + "/me"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 - Invalid / malformed token")
        void me_invalidToken_returns403() throws Exception {
            mockMvc.perform(get(BASE + "/me")
                            .header("Authorization", "Bearer invalid.token.here"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // POST /logout
    // =========================================================================

    @Nested
    @DisplayName("POST /logout - Logout")
    class LogoutTests {

        @Test
        @DisplayName("204 - Successful logout")
        void logout_authenticated_returns204() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            mockMvc.perform(post(BASE + "/logout")
                            .header("Authorization", bearerToken("alice@test.com")))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("403 - Logout without token is rejected (protected endpoint)")
        void logout_noToken_returns403() throws Exception {
            // /auth/logout is NOT part of SecurityConfig's permitAll list
            mockMvc.perform(post(BASE + "/logout"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // Admin endpoint security
    // =========================================================================

    @Nested
    @DisplayName("Admin endpoint access")
    class AdminAccessTests {

        @Test
        @DisplayName("403 - A standard user cannot access /admin/**")
        void adminEndpoint_regularUser_returns403() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            mockMvc.perform(get("/api/v1/admin/users")
                            .header("Authorization", bearerToken("alice@test.com")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 - Access without token to a protected endpoint")
        void protectedEndpoint_noToken_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/sessions/me"))
                    .andExpect(status().isForbidden());
        }
    }
}
