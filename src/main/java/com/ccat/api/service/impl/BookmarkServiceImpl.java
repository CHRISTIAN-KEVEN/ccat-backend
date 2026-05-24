package com.ccat.api.service.impl;

import com.ccat.api.dto.response.AnswerResponse;
import com.ccat.api.dto.response.BookmarkResponse;
import com.ccat.api.exception.QuestionNotFoundException;
import com.ccat.api.mapper.AnswerMapper;
import com.ccat.api.model.entity.QuestionBookmark;
import com.ccat.api.repository.AnswerRepository;
import com.ccat.api.repository.QuestionBookmarkRepository;
import com.ccat.api.repository.QuestionRepository;
import com.ccat.api.repository.UserRepository;
import com.ccat.api.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final QuestionBookmarkRepository bookmarkRepository;
    private final QuestionRepository         questionRepository;
    private final UserRepository             userRepository;
    private final AnswerRepository           answerRepository;
    private final AnswerMapper               answerMapper;

    @Override
    @Transactional
    public boolean toggle(Long questionId, String userEmail) {
        var user     = userRepository.findByStrEmail(userEmail).orElseThrow();
        var question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("id=" + questionId));

        var existing = bookmarkRepository.findByUserLgIdAndQuestionLgId(user.getLgId(), questionId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false;
        }

        var bookmark = new QuestionBookmark();
        bookmark.setUser(user);
        bookmark.setQuestion(question);
        bookmarkRepository.save(bookmark);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getMyBookmarks(String userEmail) {
        var user = userRepository.findByStrEmail(userEmail).orElseThrow();
        return bookmarkRepository.findByUserLgIdOrderByDtCreatedDesc(user.getLgId())
                .stream()
                .map(b -> {
                    var q = b.getQuestion();
                    List<AnswerResponse> answers = answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(q.getLgId())
                            .stream().map(answerMapper::toResponse).toList();
                    return new BookmarkResponse(
                        b.getLgId(),
                        q.getLgId(),
                        q.getStrQuestionText(),
                        q.getStrImageUrl(),
                        q.getDomain().getStrDomainCode(),
                        q.getEmDifficulty().name(),
                        q.getEmQuestionType().name(),
                        q.getStrExplanation(),
                        b.getStrNote(),
                        true,
                        b.getDtCreated(),
                        answers
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public BookmarkResponse updateNote(Long questionId, String note, String userEmail) {
        var user = userRepository.findByStrEmail(userEmail).orElseThrow();
        var bookmark = bookmarkRepository.findByUserLgIdAndQuestionLgId(user.getLgId(), questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Bookmark not found for question id=" + questionId));

        bookmark.setStrNote(note == null || note.isBlank() ? null : note.trim());
        bookmarkRepository.save(bookmark);

        var q = bookmark.getQuestion();
        List<AnswerResponse> answers = answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(q.getLgId())
                .stream().map(answerMapper::toResponse).toList();
        return new BookmarkResponse(
                bookmark.getLgId(), q.getLgId(), q.getStrQuestionText(), q.getStrImageUrl(),
                q.getDomain().getStrDomainCode(), q.getEmDifficulty().name(),
                q.getEmQuestionType().name(), q.getStrExplanation(),
                bookmark.getStrNote(), true, bookmark.getDtCreated(), answers
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getMyBookmarkedIds(String userEmail) {
        var user = userRepository.findByStrEmail(userEmail).orElseThrow();
        return bookmarkRepository.findBookmarkedQuestionIdsByUserId(user.getLgId());
    }
}
