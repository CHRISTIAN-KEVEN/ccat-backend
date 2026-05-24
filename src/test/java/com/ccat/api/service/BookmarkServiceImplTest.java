package com.ccat.api.service;

import com.ccat.api.exception.QuestionNotFoundException;
import com.ccat.api.mapper.AnswerMapper;
import com.ccat.api.model.entity.*;
import com.ccat.api.model.enums.*;
import com.ccat.api.repository.*;
import com.ccat.api.service.impl.BookmarkServiceImpl;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour BookmarkServiceImpl.
 * Couvre l'ajout, la suppression et la mise à jour des marque-pages.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkServiceImpl — Tests Unitaires")
class BookmarkServiceImplTest {

    @Mock QuestionBookmarkRepository bookmarkRepository;
    @Mock QuestionRepository         questionRepository;
    @Mock UserRepository             userRepository;
    @Mock AnswerRepository           answerRepository;
    @Mock AnswerMapper               answerMapper;

    @InjectMocks
    BookmarkServiceImpl service;

    private User user;
    private Question question;
    private QuestionBookmark bookmark;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLgId(1L);
        user.setStrEmail("alice@test.com");

        Domain domain = new Domain();
        domain.setStrDomainCode("VERBAL");
        domain.setStrLabel("Verbal Reasoning");

        question = new Question();
        question.setLgId(42L);
        question.setStrUuid("uuid-q-001");
        question.setDomain(domain);
        question.setEmDifficulty(QuestionDifficulty.MEDIUM);
        question.setEmQuestionType(QuestionType.MULTIPLE_CHOICE);
        question.setEmContentType(ContentType.TEXT);
        question.setStrQuestionText("Quel est le synonyme de 'rapide' ?");
        question.setBActive(true);

        bookmark = new QuestionBookmark();
        bookmark.setLgId(100L);
        bookmark.setUser(user);
        bookmark.setQuestion(question);
        bookmark.setStrNote(null);
    }

    // =========================================================================
    // toggle — Ajout / Suppression d'un marque-page
    // =========================================================================

    @Nested
    @DisplayName("toggle() — Ajout / Suppression d'un marque-page")
    class ToggleTests {

        @Test
        @DisplayName("Ajoute le marque-page si la question n'est pas encore bookmarkée → retourne true")
        void toggle_notBookmarked_addsAndReturnsTrue() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(questionRepository.findById(42L)).thenReturn(Optional.of(question));
            when(bookmarkRepository.findByUserLgIdAndQuestionLgId(1L, 42L))
                    .thenReturn(Optional.empty());
            when(bookmarkRepository.save(any())).thenReturn(bookmark);

            boolean result = service.toggle(42L, "alice@test.com");

            assertThat(result).isTrue();
            verify(bookmarkRepository).save(any(QuestionBookmark.class));
        }

        @Test
        @DisplayName("Supprime le marque-page s'il existe déjà → retourne false")
        void toggle_alreadyBookmarked_removesAndReturnsFalse() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(questionRepository.findById(42L)).thenReturn(Optional.of(question));
            when(bookmarkRepository.findByUserLgIdAndQuestionLgId(1L, 42L))
                    .thenReturn(Optional.of(bookmark));

            boolean result = service.toggle(42L, "alice@test.com");

            assertThat(result).isFalse();
            verify(bookmarkRepository).delete(bookmark);
            verify(bookmarkRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lève QuestionNotFoundException si la question n'existe pas")
        void toggle_questionNotFound_throwsException() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(questionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.toggle(99L, "alice@test.com"))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // getMyBookmarks — Liste des marque-pages
    // =========================================================================

    @Nested
    @DisplayName("getMyBookmarks() — Récupération des marque-pages")
    class GetMyBookmarksTests {

        @Test
        @DisplayName("Retourne la liste des marque-pages de l'utilisateur")
        void getMyBookmarks_withBookmarks_returnsList() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findByUserLgIdOrderByDtCreatedDesc(1L))
                    .thenReturn(List.of(bookmark));
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(42L))
                    .thenReturn(List.of());

            var result = service.getMyBookmarks("alice@test.com");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).lgId()).isEqualTo(100L);
            assertThat(result.get(0).lgQuestionId()).isEqualTo(42L);
            assertThat(result.get(0).bBookmarked()).isTrue();
        }

        @Test
        @DisplayName("Retourne une liste vide si aucun marque-page")
        void getMyBookmarks_noBookmarks_returnsEmptyList() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findByUserLgIdOrderByDtCreatedDesc(1L))
                    .thenReturn(List.of());

            var result = service.getMyBookmarks("alice@test.com");

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // updateNote — Mise à jour de la note
    // =========================================================================

    @Nested
    @DisplayName("updateNote() — Mise à jour de la note d'un marque-page")
    class UpdateNoteTests {

        @Test
        @DisplayName("Met à jour la note avec le texte fourni")
        void updateNote_validNote_updatesNote() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findByUserLgIdAndQuestionLgId(1L, 42L))
                    .thenReturn(Optional.of(bookmark));
            when(bookmarkRepository.save(any())).thenReturn(bookmark);
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(42L))
                    .thenReturn(List.of());

            service.updateNote(42L, "Ma note personnelle", "alice@test.com");

            assertThat(bookmark.getStrNote()).isEqualTo("Ma note personnelle");
            verify(bookmarkRepository).save(bookmark);
        }

        @Test
        @DisplayName("Met la note à null si le texte est vide ou blank")
        void updateNote_blankNote_setsNoteToNull() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findByUserLgIdAndQuestionLgId(1L, 42L))
                    .thenReturn(Optional.of(bookmark));
            when(bookmarkRepository.save(any())).thenReturn(bookmark);
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(42L))
                    .thenReturn(List.of());

            service.updateNote(42L, "   ", "alice@test.com");

            assertThat(bookmark.getStrNote()).isNull();
        }

        @Test
        @DisplayName("Lève une exception si le marque-page n'existe pas pour cet utilisateur")
        void updateNote_bookmarkNotFound_throwsException() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findByUserLgIdAndQuestionLgId(1L, 99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateNote(99L, "Note", "alice@test.com"))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // getMyBookmarkedIds — IDs bookmarkés
    // =========================================================================

    @Nested
    @DisplayName("getMyBookmarkedIds() — Récupération des IDs bookmarkés")
    class GetMyBookmarkedIdsTests {

        @Test
        @DisplayName("Retourne le Set des IDs de questions bookmarkées")
        void getMyBookmarkedIds_returnsIds() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findBookmarkedQuestionIdsByUserId(1L))
                    .thenReturn(Set.of(42L, 55L, 78L));

            var result = service.getMyBookmarkedIds("alice@test.com");

            assertThat(result).containsExactlyInAnyOrder(42L, 55L, 78L);
        }

        @Test
        @DisplayName("Retourne un Set vide si aucune question bookmarkée")
        void getMyBookmarkedIds_noBookmarks_returnsEmptySet() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findBookmarkedQuestionIdsByUserId(1L))
                    .thenReturn(Set.of());

            var result = service.getMyBookmarkedIds("alice@test.com");

            assertThat(result).isEmpty();
        }
    }
}
