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
 * Unit tests for BookmarkServiceImpl.
 * Covers bookmark creation, removal, and note updates.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkServiceImpl - Unit Tests")
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
        question.setStrQuestionText("What is the synonym of 'fast'?");
        question.setBActive(true);

        bookmark = new QuestionBookmark();
        bookmark.setLgId(100L);
        bookmark.setUser(user);
        bookmark.setQuestion(question);
        bookmark.setStrNote(null);
    }

    // =========================================================================
    // toggle - add or remove a bookmark
    // =========================================================================

    @Nested
    @DisplayName("toggle() - Adds or removes a bookmark")
    class ToggleTests {

        @Test
        @DisplayName("Adds the bookmark when the question is not bookmarked yet and returns true")
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
        @DisplayName("Removes the bookmark when it already exists and returns false")
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
        @DisplayName("Throws QuestionNotFoundException when the question does not exist")
        void toggle_questionNotFound_throwsException() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(questionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.toggle(99L, "alice@test.com"))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // getMyBookmarks - list bookmarks
    // =========================================================================

    @Nested
    @DisplayName("getMyBookmarks() - Retrieves bookmarks")
    class GetMyBookmarksTests {

        @Test
        @DisplayName("Returns the user's bookmark list")
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
        @DisplayName("Returns an empty list when there are no bookmarks")
        void getMyBookmarks_noBookmarks_returnsEmptyList() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findByUserLgIdOrderByDtCreatedDesc(1L))
                    .thenReturn(List.of());

            var result = service.getMyBookmarks("alice@test.com");

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // updateNote - update bookmark note
    // =========================================================================

    @Nested
    @DisplayName("updateNote() - Updates a bookmark note")
    class UpdateNoteTests {

        @Test
        @DisplayName("Updates the note with the provided text")
        void updateNote_validNote_updatesNote() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findByUserLgIdAndQuestionLgId(1L, 42L))
                    .thenReturn(Optional.of(bookmark));
            when(bookmarkRepository.save(any())).thenReturn(bookmark);
            when(answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(42L))
                    .thenReturn(List.of());

            service.updateNote(42L, "My personal note", "alice@test.com");

            assertThat(bookmark.getStrNote()).isEqualTo("My personal note");
            verify(bookmarkRepository).save(bookmark);
        }

        @Test
        @DisplayName("Sets the note to null when the text is empty or blank")
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
        @DisplayName("Throws an exception when the bookmark does not exist for this user")
        void updateNote_bookmarkNotFound_throwsException() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findByUserLgIdAndQuestionLgId(1L, 99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateNote(99L, "Note", "alice@test.com"))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    // =========================================================================
    // getMyBookmarkedIds - bookmarked IDs
    // =========================================================================

    @Nested
    @DisplayName("getMyBookmarkedIds() - Retrieves bookmarked IDs")
    class GetMyBookmarkedIdsTests {

        @Test
        @DisplayName("Returns the set of bookmarked question IDs")
        void getMyBookmarkedIds_returnsIds() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findBookmarkedQuestionIdsByUserId(1L))
                    .thenReturn(Set.of(42L, 55L, 78L));

            var result = service.getMyBookmarkedIds("alice@test.com");

            assertThat(result).containsExactlyInAnyOrder(42L, 55L, 78L);
        }

        @Test
        @DisplayName("Returns an empty set when there are no bookmarked questions")
        void getMyBookmarkedIds_noBookmarks_returnsEmptySet() {
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(bookmarkRepository.findBookmarkedQuestionIdsByUserId(1L))
                    .thenReturn(Set.of());

            var result = service.getMyBookmarkedIds("alice@test.com");

            assertThat(result).isEmpty();
        }
    }
}
