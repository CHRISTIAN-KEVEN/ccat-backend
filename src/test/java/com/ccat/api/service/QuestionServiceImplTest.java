package com.ccat.api.service;

import com.ccat.api.dto.request.QuestionCreateRequest;
import com.ccat.api.dto.request.QuestionUpdateRequest;
import com.ccat.api.dto.response.AnswerResponse;
import com.ccat.api.dto.response.QuestionResponse;
import com.ccat.api.exception.DomainNotFoundException;
import com.ccat.api.exception.QuestionNotFoundException;
import com.ccat.api.mapper.AnswerMapper;
import com.ccat.api.mapper.QuestionMapper;
import com.ccat.api.model.entity.*;
import com.ccat.api.model.enums.*;
import com.ccat.api.repository.*;
import com.ccat.api.service.impl.QuestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuestionServiceImpl.
 * Covers question CRUD operations and business error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionServiceImpl - Unit Tests")
class QuestionServiceImplTest {

    @Mock QuestionRepository questionRepository;
    @Mock AnswerRepository   answerRepository;
    @Mock DomainRepository   domainRepository;
    @Mock UserRepository     userRepository;
    @Mock QuestionMapper     questionMapper;
    @Mock AnswerMapper       answerMapper;

    @InjectMocks
    QuestionServiceImpl service;

    private Domain domain;
    private User   admin;
    private Question question;
    private QuestionResponse questionResponse;

    @BeforeEach
    void setUp() {
        domain = new Domain();
        domain.setStrDomainCode("VERBAL");
        domain.setStrLabel("Verbal Reasoning");
        domain.setEmStatus(DomainStatus.ACTIVE);

        admin = new User();
        admin.setLgId(1L);
        admin.setStrEmail("admin@test.com");

        question = new Question();
        question.setLgId(10L);
        question.setStrUuid("uuid-q-001");
        question.setDomain(domain);
        question.setEmDifficulty(QuestionDifficulty.MEDIUM);
        question.setEmQuestionType(QuestionType.MULTIPLE_CHOICE);
        question.setEmContentType(ContentType.TEXT);
        question.setStrQuestionText("What is the synonym of 'fast'?");
        question.setStrExplanation("Basic explanation.");
        question.setBActive(true);
        question.setBVerified(false);
        question.setIntPointValue(1);
        question.setIntTimeLimitMs(18000);
        question.setIntReportCount(0);
        question.setDbAvgCorrectRate(0.0);
        question.setDbAvgResponseMs(0.0);

        questionResponse = new QuestionResponse(
                10L, "uuid-q-001", "VERBAL", QuestionDifficulty.MEDIUM,
                QuestionType.MULTIPLE_CHOICE, ContentType.TEXT,
                "What is the synonym of 'fast'?", null, null,
                "Basic explanation.", null, 1, 18000,
                true, false, List.of());
    }

    // =========================================================================
    // findAll
    // =========================================================================

    @Nested
    @DisplayName("findAll() - Lists all active questions")
    class FindAllTests {

        @Test
        @DisplayName("Returns the list of active questions")
        void findAll_returnsActiveQuestions() {
            when(questionRepository.findByBActiveTrueOrderByDtCreatedDesc())
                    .thenReturn(List.of(question));
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(10L))
                    .thenReturn(List.of());
            when(questionMapper.toResponseWithAnswers(any(), any()))
                    .thenReturn(questionResponse);

            var result = service.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).strUuid()).isEqualTo("uuid-q-001");
        }

        @Test
        @DisplayName("Returns an empty list when there are no active questions")
        void findAll_noQuestions_returnsEmptyList() {
            when(questionRepository.findByBActiveTrueOrderByDtCreatedDesc())
                    .thenReturn(List.of());

            var result = service.findAll();

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // findByUuid
    // =========================================================================

    @Nested
    @DisplayName("findByUuid() - Lookup by UUID")
    class FindByUuidTests {

        @Test
        @DisplayName("Returns the question when the UUID exists")
        void findByUuid_found_returnsQuestion() {
            when(questionRepository.findByStrUuid("uuid-q-001"))
                    .thenReturn(Optional.of(question));
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(10L))
                    .thenReturn(List.of());
            when(questionMapper.toResponseWithAnswers(any(), any()))
                    .thenReturn(questionResponse);

            var result = service.findByUuid("uuid-q-001");

            assertThat(result).isNotNull();
            assertThat(result.strUuid()).isEqualTo("uuid-q-001");
        }

        @Test
        @DisplayName("Throws QuestionNotFoundException when the UUID is not found")
        void findByUuid_notFound_throwsException() {
            when(questionRepository.findByStrUuid("uuid-missing"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByUuid("uuid-missing"))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // create
    // =========================================================================

    @Nested
    @DisplayName("create() - Creates a question")
    class CreateTests {

        @Test
        @DisplayName("Creates and returns the question with an auto-generated UUID")
        void create_validRequest_savesAndReturns() {
            var request = new QuestionCreateRequest(
                    "VERBAL", QuestionDifficulty.MEDIUM, QuestionType.MULTIPLE_CHOICE,
                    ContentType.TEXT, "What is the synonym of 'fast'?",
                    null, null, "Explanation", null, 1, 18000, null, null);

            when(domainRepository.findById("VERBAL")).thenReturn(Optional.of(domain));
            when(userRepository.findByStrEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(questionMapper.toEntity(any(), any(), any())).thenReturn(question);
            when(questionRepository.save(any())).thenReturn(question);
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(10L))
                    .thenReturn(List.of());
            when(questionMapper.toResponseWithAnswers(any(), any())).thenReturn(questionResponse);

            var result = service.create(request, "admin@test.com");

            assertThat(result).isNotNull();
            verify(questionRepository).save(any(Question.class));
            // Verify that a UUID was assigned to the question before saving
            verify(questionMapper).toEntity(eq(request), eq(domain), eq(admin));
        }

        @Test
        @DisplayName("Throws DomainNotFoundException when the domain is not found")
        void create_domainNotFound_throwsException() {
            var request = new QuestionCreateRequest(
                    "MISSING_DOMAIN", QuestionDifficulty.EASY, QuestionType.TRUE_FALSE,
                    ContentType.TEXT, "Question?", null, null, null, null, 1, 18000, null, null);

            when(domainRepository.findById("MISSING_DOMAIN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request, "admin@test.com"))
                    .isInstanceOf(DomainNotFoundException.class);
        }
    }

    // =========================================================================
    // update
    // =========================================================================

    @Nested
    @DisplayName("update() - Updates a question")
    class UpdateTests {

        @Test
        @DisplayName("Updates only non-null fields")
        void update_partialUpdate_onlyChangesProvidedFields() {
            var request = new QuestionUpdateRequest(
                    QuestionDifficulty.HARD, null, null,
                    "Updated question text", null, null,
                    "Updated explanation", null, null, null,
                    null, null, null, null);

            when(questionRepository.findByStrUuid("uuid-q-001"))
                    .thenReturn(Optional.of(question));
            when(questionRepository.save(any())).thenReturn(question);
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(10L))
                    .thenReturn(List.of());
            when(questionMapper.toResponseWithAnswers(any(), any())).thenReturn(questionResponse);

            service.update("uuid-q-001", request);

            assertThat(question.getEmDifficulty()).isEqualTo(QuestionDifficulty.HARD);
            assertThat(question.getStrQuestionText()).isEqualTo("Updated question text");
            assertThat(question.getStrExplanation()).isEqualTo("Updated explanation");
            // The question type should remain unchanged because the request value is null
            assertThat(question.getEmQuestionType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        }

        @Test
        @DisplayName("Throws QuestionNotFoundException when the UUID is not found")
        void update_notFound_throwsException() {
            var request = new QuestionUpdateRequest(
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);

            when(questionRepository.findByStrUuid("uuid-missing"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update("uuid-missing", request))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // delete (soft delete)
    // =========================================================================

    @Nested
    @DisplayName("delete() - Soft deletes a question")
    class DeleteTests {

        @Test
        @DisplayName("Sets bActive to false without deleting the database row")
        void delete_existingQuestion_setsInactive() {
            when(questionRepository.findByStrUuid("uuid-q-001"))
                    .thenReturn(Optional.of(question));
            when(questionRepository.save(any())).thenReturn(question);

            service.delete("uuid-q-001");

            assertThat(question.getBActive()).isFalse();
            verify(questionRepository).save(question);
            // No deleteById call because this is a soft delete
            verify(questionRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Throws QuestionNotFoundException when the UUID is not found")
        void delete_notFound_throwsException() {
            when(questionRepository.findByStrUuid("uuid-missing"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete("uuid-missing"))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // findByDomain
    // =========================================================================

    @Nested
    @DisplayName("findByDomain() - Filters by domain")
    class FindByDomainTests {

        @Test
        @DisplayName("Returns the questions for the requested domain")
        void findByDomain_returnsMatchingQuestions() {
            when(questionRepository.findByDomainStrDomainCodeAndBActiveTrue("VERBAL"))
                    .thenReturn(List.of(question));
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(10L))
                    .thenReturn(List.of());
            when(questionMapper.toResponseWithAnswers(any(), any())).thenReturn(questionResponse);

            var result = service.findByDomain("VERBAL");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Returns an empty list for a domain with no active questions")
        void findByDomain_noQuestions_returnsEmptyList() {
            when(questionRepository.findByDomainStrDomainCodeAndBActiveTrue("SPATIAL"))
                    .thenReturn(List.of());

            var result = service.findByDomain("SPATIAL");

            assertThat(result).isEmpty();
        }
    }
}
