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
 * Base class for all integration tests.
 *
 * - "test" profile -> in-memory H2 (application-test.properties)
 * - @MockitoBean on JavaMailSender -> no real email is sent
 * - Helpers: user/domain/question creation and JWT generation
 *
 * Spring Boot 4 notes:
 *   @AutoConfigureMockMvc -> org.springframework.boot.webmvc.test.autoconfigure
 *   @MockitoBean          -> org.springframework.test.context.bean.override.mockito
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

    // Repositories used to prepare test data
    @Autowired protected UserRepository     userRepository;
    @Autowired protected DomainRepository   domainRepository;
    @Autowired protected QuestionRepository questionRepository;
    @Autowired protected AnswerRepository   answerRepository;

    // Mocked external service: no real email delivery
    @MockitoBean
    protected JavaMailSender mailSender;

    // =========================================================================
    // Helpers - test entity creation
    // =========================================================================

    /** Creates a standard verified user (USER role) in the test database. */
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

    /** Creates a verified administrator user (ADMIN role) in the test database. */
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

    /** Generates a valid JWT token in the form "Bearer <token>". */
    protected String bearerToken(String email) {
        return "Bearer " + jwtService.generateToken(email);
    }

    /** Creates an active domain in the test database (idempotent on code). */
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

    /** Creates an active question with 2 answers (one correct, one incorrect). */
    protected Question createQuestion(Domain domain, User createdBy) {
        Question q = new Question();
        q.setStrUuid(UUID.randomUUID().toString());
        q.setDomain(domain);
        q.setEmDifficulty(QuestionDifficulty.MEDIUM);
        q.setEmQuestionType(QuestionType.MULTIPLE_CHOICE);
        q.setEmContentType(ContentType.TEXT);
        q.setStrQuestionText("Test question number " + UUID.randomUUID());
        q.setStrExplanation("Test explanation.");
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

    /** Creates an answer for a question.
     *  @param label     Short option identifier (max 5 chars): "A", "B", "True", ...
     *  @param text      Full text shown to the user.
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

    /** Convenience overload that creates an answer with generic text for tests
     *  that do not need specific content. */
    protected Answer createAnswer(Question question, String label,
                                   boolean isCorrect, int sortOrder) {
        return createAnswer(question, label, "Answer " + label, isCorrect, sortOrder);
    }

    /** Serializes an object to JSON. */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
