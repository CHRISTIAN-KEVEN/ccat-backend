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
 * Tests d'intégration pour AuthController.
 * Utilise H2 en mémoire + MockMvc. Aucun email réel envoyé.
 *
 * Endpoints couverts :
 *   POST /api/v1/auth/register
 *   POST /api/v1/auth/login
 *   GET  /api/v1/auth/me
 *   POST /api/v1/auth/logout
 */
@DisplayName("AuthController — Tests d'intégration")
class AuthControllerIT extends IntegrationTestBase {

    private static final String BASE = "/api/v1/auth";

    // =========================================================================
    // POST /register
    // =========================================================================

    @Nested
    @DisplayName("POST /register — Inscription")
    class RegisterTests {

        @Test
        @DisplayName("201 — Inscription réussie avec des données valides")
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
        @DisplayName("409 — Email déjà utilisé")
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
        @DisplayName("400 — Email invalide")
        void register_invalidEmail_returns400() throws Exception {
            var request = new UserRegisterRequest(
                    "pas-un-email", "Str0ng!Pass", "Alice", "Dupont", "en");

            mockMvc.perform(post(BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — Mot de passe trop court (< 8 caractères)")
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
    @DisplayName("POST /login — Connexion")
    class LoginTests {

        @Test
        @DisplayName("200 — Connexion réussie avec bon email/mot de passe")
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
        @DisplayName("401 — Mauvais mot de passe")
        void login_wrongPassword_returns401() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            var request = new UserLoginRequest("alice@test.com", "WrongPass!");

            mockMvc.perform(post(BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 — Email inexistant")
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
    @DisplayName("GET /me — Profil utilisateur connecté")
    class MeTests {

        @Test
        @DisplayName("200 — Retourne le profil de l'utilisateur connecté")
        void me_authenticated_returnsUserProfile() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            mockMvc.perform(get(BASE + "/me")
                            .header("Authorization", bearerToken("alice@test.com")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.strEmail").value("alice@test.com"));
        }

        @Test
        @DisplayName("403 — Aucun token fourni (Spring Security 6 retourne 403 sans AuthenticationEntryPoint)")
        void me_noToken_returns403() throws Exception {
            mockMvc.perform(get(BASE + "/me"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 — Token invalide / malformé")
        void me_invalidToken_returns403() throws Exception {
            mockMvc.perform(get(BASE + "/me")
                            .header("Authorization", "Bearer token.invalide.ici"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // POST /logout
    // =========================================================================

    @Nested
    @DisplayName("POST /logout — Déconnexion")
    class LogoutTests {

        @Test
        @DisplayName("204 — Déconnexion réussie")
        void logout_authenticated_returns204() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            mockMvc.perform(post(BASE + "/logout")
                            .header("Authorization", bearerToken("alice@test.com")))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("403 — Logout sans token refusé (endpoint protégé)")
        void logout_noToken_returns403() throws Exception {
            // /auth/logout n'est PAS dans la liste permitAll de SecurityConfig
            mockMvc.perform(post(BASE + "/logout"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // Sécurité des endpoints admin
    // =========================================================================

    @Nested
    @DisplayName("Accès aux endpoints admin")
    class AdminAccessTests {

        @Test
        @DisplayName("403 — Un utilisateur standard ne peut pas accéder à /admin/**")
        void adminEndpoint_regularUser_returns403() throws Exception {
            createUser("alice@test.com", "Str0ng!Pass");

            mockMvc.perform(get("/api/v1/admin/users")
                            .header("Authorization", bearerToken("alice@test.com")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 — Accès sans token à un endpoint protégé")
        void protectedEndpoint_noToken_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/sessions/me"))
                    .andExpect(status().isForbidden());
        }
    }
}
