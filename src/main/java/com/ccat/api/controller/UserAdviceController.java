package com.ccat.api.controller;

import com.ccat.api.dto.request.UserAdviceFeedbackRequest;
import com.ccat.api.dto.response.UserAdviceResponse;
import com.ccat.api.service.UserAdviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Personalized Advice", description = "Post-test personalized advice cards")
public class UserAdviceController {

    private final UserAdviceService userAdviceService;

    @Operation(summary = "Get advice cards for a submitted session",
               description = "Returns personalized advice generated from the test result. Only available after the session is submitted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Advice cards ordered by priority",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserAdviceResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/api/v1/sessions/{sessionId}/advices")
    public ResponseEntity<List<UserAdviceResponse>> getBySession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userAdviceService.getBySession(sessionId, principal.getUsername()));
    }

    @Operation(summary = "Mark an advice card as read")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Marked as read",
                    content = @Content(schema = @Schema(implementation = UserAdviceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advice not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/api/v1/advices/{id}/read")
    public ResponseEntity<UserAdviceResponse> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userAdviceService.markRead(id, principal.getUsername()));
    }

    @Operation(summary = "Submit feedback on an advice card",
               description = "Records whether the advice was helpful, time spent, and any user note.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feedback recorded",
                    content = @Content(schema = @Schema(implementation = UserAdviceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Advice not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/api/v1/advices/feedback")
    public ResponseEntity<UserAdviceResponse> submitFeedback(
            @Valid @RequestBody UserAdviceFeedbackRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userAdviceService.submitFeedback(request, principal.getUsername()));
    }
}
