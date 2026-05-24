package com.ccat.api.util;

import com.ccat.api.model.entity.*;
import com.ccat.api.model.enums.*;
import com.ccat.api.repository.*;
import com.ccat.api.security.JwtService;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Classe de base pour tous les tests d'intégration.
 *
 * - Profil "test" → H2 en mémoire (application-test.properties)
 * - @MockitoBean sur JavaMailSender → aucun email réel envoyé
 * - Helpers : création d'utilisateur / domaine / question, génération JWT
 *
 * Spring Boot 4 notes :
 *   @AutoConfigureMockMvc → org.springframework.boot.webmvc.test.autoconfigure
 *   @MockitoBean          → org.springframework.test.context.bean.override.mockito
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class IntegrationTestBase {

    @Autowired protected MockMvc        mockMvc;
    @Autowired protected ObjectMapper   objectMapper;
    @Autowired protected JwtService     jwtService;

    // ── Repositories pour préparer les données de test ───────────────────────
    @Autowired protected UserRepository     userRepository;
    @Autowired protected DomainRepository   domainRepository;
    @Autowired protected QuestionRepository questionRepository;
    @Autowired protected AnswerRepository   answerRepository;

    // ── Service externe mocké : pas d'envoi réel d'emails ────────────────────
    @MockitoBean
    protected JavaMailSender mailSender;

    // =========================================================================
    // Helpers — Création des entités de test
    // =========================================================================

    /** Crée un utilisateur standard (rôle USER) vérifié en base de test. */
    protected User createUser(String email, String password) {
        User user = new User();
        user.setStrUuid(UUID.randomUUID().toString());
        user.setStrEmail(email);
        user.setStrPasswordHash(
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                    .encode(password));
        user.setEmRole(UserRole.USER);
        user.setEmStatus(UserStatus.ACTIVE);
        user.setBEmailVerified(true);
        return userRepository.save(user);
    }

    /** Crée un utilisateur administrateur (rôle ADMIN) vérifié en base de test. */
    protected User createAdmin(String email, String password) {
        User admin = new User();
        admin.setStrUuid(UUID.randomUUID().toString());
        admin.setStrEmail(email);
        admin.setStrPasswordHash(
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                    .encode(password));
        admin.setEmRole(UserRole.ADMIN);
        admin.setEmStatus(UserStatus.ACTIVE);
        admin.setBEmailVerified(true);
        return userRepository.save(admin);
    }

    /** Génère un token JWT valide sous la forme "Bearer <token>". */
    protected String bearerToken(String email) {
        return "Bearer " + jwtService.generateToken(email);
    }

    /** Crée un domaine actif en base de test (idempotent sur le code). */
    protected Domain createDomain(String code, String label) {
        return domainRepository.findById(code).orElseGet(() -> {
            Domain d = new Domain();
            d.setStrDomainCode(code);
            d.setStrLabel(label);
            d.setEmStatus(DomainStatus.ACTIVE);
            d.setIntSortOrder(1);
            return domainRepository.save(d);
        });
    }

    /** Crée une question active avec 2 réponses (une correcte, une incorrecte). */
    protected Question createQuestion(Domain domain, User createdBy) {
        Question q = new Question();
        q.setStrUuid(UUID.randomUUID().toString());
        q.setDomain(domain);
        q.setEmDifficulty(QuestionDifficulty.MEDIUM);
        q.setEmQuestionType(QuestionType.MULTIPLE_CHOICE);
        q.setEmContentType(ContentType.TEXT);
        q.setStrQuestionText("Question de test numéro " + UUID.randomUUID());
        q.setStrExplanation("Explication de test.");
        q.setBActive(true);
        q.setBVerified(true);
        q.setIntPointValue(1);
        q.setIntTimeLimitMs(18000);
        q.setIntReportCount(0);
        q.setDbAvgCorrectRate(0.0);
        q.setDbAvgResponseMs(0.0);
        q.setCreatedBy(createdBy);
        return questionRepository.save(q);
    }

    /** Crée une réponse pour une question.
     *  @param label     Court identifiant de l'option (max 5 car.) : "A", "B", "Vrai"…
     *  @param text      Texte complet affiché à l'utilisateur.
     */
    protected Answer createAnswer(Question question, String label, String text,
                                   boolean isCorrect, int sortOrder) {
        Answer a = new Answer();
        a.setQuestion(question);
        a.setStrAnswerLabel(label);
        a.setStrAnswerText(text);
        a.setBIsCorrect(isCorrect);
        a.setIntSortOrder(sortOrder);
        return answerRepository.save(a);
    }

    /** Raccourci : crée une réponse avec un texte générique (utile pour les tests
     *  qui n'ont pas besoin d'un contenu précis). */
    protected Answer createAnswer(Question question, String label,
                                   boolean isCorrect, int sortOrder) {
        return createAnswer(question, label, "Réponse " + label, isCorrect, sortOrder);
    }

    /** Sérialise un objet en JSON. */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
