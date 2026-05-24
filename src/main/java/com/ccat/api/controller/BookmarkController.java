package com.ccat.api.controller;

import com.ccat.api.dto.response.BookmarkResponse;
import com.ccat.api.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmarks", description = "Save and review bookmarked questions")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "Toggle bookmark on a question (add if absent, remove if present)")
    @PostMapping("/{questionId}/toggle")
    public ResponseEntity<Map<String, Boolean>> toggle(
            @PathVariable Long questionId,
            @AuthenticationPrincipal UserDetails principal) {
        boolean bookmarked = bookmarkService.toggle(questionId, principal.getUsername());
        return ResponseEntity.ok(Map.of("bBookmarked", bookmarked));
    }

    @Operation(summary = "Get all bookmarked questions for the current user")
    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getMyBookmarks(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(bookmarkService.getMyBookmarks(principal.getUsername()));
    }

    @Operation(summary = "Add or update a personal note on a bookmarked question")
    @PatchMapping("/{questionId}/note")
    public ResponseEntity<BookmarkResponse> updateNote(
            @PathVariable Long questionId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(bookmarkService.updateNote(questionId, body.get("strNote"), principal.getUsername()));
    }

    @Operation(summary = "Get the set of bookmarked question IDs (for highlighting in UI)")
    @GetMapping("/ids")
    public ResponseEntity<Set<Long>> getMyBookmarkedIds(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(bookmarkService.getMyBookmarkedIds(principal.getUsername()));
    }
}
