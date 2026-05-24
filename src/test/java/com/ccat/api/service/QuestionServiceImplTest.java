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
 * Tests unitaires pour QuestionServiceImpl.
 * Couvre le CRUD des questions et la gestion des erreurs métier.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionServiceImpl — Tests Unitaires")
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
        question.setStrQuestionText("Quel est le synonyme de 'rapide' ?");
        question.setStrExplanation("Explication de base.");
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
                "Quel est le synonyme de 'rapide' ?", null, null,
                "Explication de base.", null, 1, 18000,
                true, false, List.of());
    }

    // =========================================================================
    // findAll
    // =========================================================================

    @Nested
    @DisplayName("findAll() — Liste toutes les questions actives")
    class FindAllTests {

        @Test
        @DisplayName("Retourne la liste des questions actives")
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
        @DisplayName("Retourne une liste vide si aucune question active")
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
    @DisplayName("findByUuid() — Recherche par UUID")
    class FindByUuidTests {

        @Test
        @DisplayName("Retourne la question si l'UUID existe")
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
        @DisplayName("Lève QuestionNotFoundException si l'UUID est introuvable")
        void findByUuid_notFound_throwsException() {
            when(questionRepository.findByStrUuid("uuid-inexistant"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByUuid("uuid-inexistant"))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // create
    // =========================================================================

    @Nested
    @DisplayName("create() — Création d'une question")
    class CreateTests {

        @Test
        @DisplayName("Crée et retourne la question avec un UUID généré automatiquement")
        void create_validRequest_savesAndReturns() {
            var request = new QuestionCreateRequest(
                    "VERBAL", QuestionDifficulty.MEDIUM, QuestionType.MULTIPLE_CHOICE,
                    ContentType.TEXT, "Quel est le synonyme de 'rapide' ?",
                    null, null, "Explication", null, 1, 18000, null, null);

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
            // Vérifier qu'un UUID a été assigné à la question avant la sauvegarde
            verify(questionMapper).toEntity(eq(request), eq(domain), eq(admin));
        }

        @Test
        @DisplayName("Lève DomainNotFoundException si le domaine est introuvable")
        void create_domainNotFound_throwsException() {
            var request = new QuestionCreateRequest(
                    "DOMAINE_INEXISTANT", QuestionDifficulty.EASY, QuestionType.TRUE_FALSE,
                    ContentType.TEXT, "Question ?", null, null, null, null, 1, 18000, null, null);

            when(domainRepository.findById("DOMAINE_INEXISTANT")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request, "admin@test.com"))
                    .isInstanceOf(DomainNotFoundException.class);
        }
    }

    // =========================================================================
    // update
    // =========================================================================

    @Nested
    @DisplayName("update() — Mise à jour d'une question")
    class UpdateTests {

        @Test
        @DisplayName("Met à jour uniquement les champs non-null")
        void update_partialUpdate_onlyChangesProvidedFields() {
            var request = new QuestionUpdateRequest(
                    QuestionDifficulty.HARD, null, null,
                    "Nouveau texte de question", null, null,
                    "Nouvelle explication", null, null, null,
                    null, null, null, null);

            when(questionRepository.findByStrUuid("uuid-q-001"))
                    .thenReturn(Optional.of(question));
            when(questionRepository.save(any())).thenReturn(question);
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(10L))
                    .thenReturn(List.of());
            when(questionMapper.toResponseWithAnswers(any(), any())).thenReturn(questionResponse);

            service.update("uuid-q-001", request);

            assertThat(question.getEmDifficulty()).isEqualTo(QuestionDifficulty.HARD);
            assertThat(question.getStrQuestionText()).isEqualTo("Nouveau texte de question");
            assertThat(question.getStrExplanation()).isEqualTo("Nouvelle explication");
            // Le type de question n'a pas changé (null dans la requête)
            assertThat(question.getEmQuestionType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        }

        @Test
        @DisplayName("Lève QuestionNotFoundException si l'UUID est introuvable")
        void update_notFound_throwsException() {
            var request = new QuestionUpdateRequest(
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);

            when(questionRepository.findByStrUuid("uuid-inexistant"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update("uuid-inexistant", request))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // delete (soft delete)
    // =========================================================================

    @Nested
    @DisplayName("delete() — Suppression logique d'une question")
    class DeleteTests {

        @Test
        @DisplayName("Met bActive à false sans supprimer la ligne en base")
        void delete_existingQuestion_setsInactive() {
            when(questionRepository.findByStrUuid("uuid-q-001"))
                    .thenReturn(Optional.of(question));
            when(questionRepository.save(any())).thenReturn(question);

            service.delete("uuid-q-001");

            assertThat(question.getBActive()).isFalse();
            verify(questionRepository).save(question);
            // Pas de deleteById — c'est un soft delete
            verify(questionRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Lève QuestionNotFoundException si l'UUID est introuvable")
        void delete_notFound_throwsException() {
            when(questionRepository.findByStrUuid("uuid-inexistant"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete("uuid-inexistant"))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // findByDomain
    // =========================================================================

    @Nested
    @DisplayName("findByDomain() — Filtrage par domaine")
    class FindByDomainTests {

        @Test
        @DisplayName("Retourne les questions du domaine demandé")
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
        @DisplayName("Retourne une liste vide pour un domaine sans questions actives")
        void findByDomain_noQuestions_returnsEmptyList() {
            when(questionRepository.findByDomainStrDomainCodeAndBActiveTrue("SPATIAL"))
                    .thenReturn(List.of());

            var result = service.findByDomain("SPATIAL");

            assertThat(result).isEmpty();
        }
    }
}
