package com.ccat.api.service;

import com.ccat.api.dto.response.BookmarkResponse;

import java.util.List;
import java.util.Set;

public interface BookmarkService {
    boolean toggle(Long questionId, String userEmail);
    List<BookmarkResponse> getMyBookmarks(String userEmail);
    Set<Long> getMyBookmarkedIds(String userEmail);
}
