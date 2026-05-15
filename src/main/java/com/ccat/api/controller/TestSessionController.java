package com.ccat.api.controller;

import com.ccat.api.dto.request.ResponseSubmitRequest;
import com.ccat.api.dto.request.TestSessionCreateRequest;
import com.ccat.api.dto.response.QuestionResponse;
import com.ccat.api.dto.response.ResponseSubmitResponse;
import com.ccat.api.dto.response.TestResultResponse;
import com.ccat.api.dto.response.TestSessionResponse;
import com.ccat.api.service.TestSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Test Sessions", description = "Start, answer and finish CCAT test sessions")
public class TestSessionController {

    private final TestSessionService sessionService;

    @Operation(summary = "Start a new test session",
               description = "Selects 50 random questions according to the domain ratio and starts the 15-minute timer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session started",
                    content = @Content(schema = @Schema(implementation = TestSessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "402", description = "Free test already used — upgrade required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<TestSessionResponse> start(
            @Valid @RequestBody TestSessionCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.start(request, principal.getUsername()));
    }

    @Operation(summary = "Submit a response to a question",
               description = "Records or updates the user's answer. Pass lgAnswerId=null to skip the question.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Response recorded",
                    content = @Content(schema = @Schema(implementation = ResponseSubmitResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session or question not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Session already submitted or expired",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{sessionId}/responses")
    public ResponseEntity<ResponseSubmitResponse> submitResponse(
            @PathVariable Long sessionId,
            @Valid @RequestBody ResponseSubmitRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(sessionService.submitResponse(sessionId, request, principal.getUsername()));
    }

    @Operation(summary = "Finish the session and compute results",
               description = "Closes the session and immediately returns the full score, accuracy, pace rating and domain breakdown.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Results computed",
                    content = @Content(schema = @Schema(implementation = TestResultResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Session already submitted",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<TestResultResponse> finish(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "false") boolean timerExpired,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(sessionService.finish(sessionId, timerExpired, principal.getUsername()));
    }

    @Operation(summary = "Get results of a submitted session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Results found",
                    content = @Content(schema = @Schema(implementation = TestResultResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found or results not yet available",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<TestResultResponse> getResult(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(sessionService.getResult(sessionId, principal.getUsername()));
    }

    @Operation(summary = "Get session details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session found",
                    content = @Content(schema = @Schema(implementation = TestSessionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{sessionId}")
    public ResponseEntity<TestSessionResponse> getById(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(sessionService.getById(sessionId, principal.getUsername()));
    }

    @Operation(summary = "Get my test session history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session history",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TestSessionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<List<TestSessionResponse>> getMyHistory(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(sessionService.getMyHistory(principal.getUsername()));
    }

    @Operation(summary = "Get questions for a session in order")
    @ApiResponse(responseCode = "200", description = "Questions with answers",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuestionResponse.class))))
    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<List<QuestionResponse>> getSessionQuestions(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(sessionService.getSessionQuestions(sessionId, principal.getUsername()));
    }
}
