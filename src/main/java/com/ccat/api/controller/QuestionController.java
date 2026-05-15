package com.ccat.api.controller;

import com.ccat.api.dto.request.AnswerCreateRequest;
import com.ccat.api.dto.request.QuestionCreateRequest;
import com.ccat.api.dto.request.QuestionImportRequest;
import com.ccat.api.dto.request.QuestionUpdateRequest;
import com.ccat.api.dto.response.AnswerResponse;
import com.ccat.api.dto.response.QuestionResponse;
import com.ccat.api.service.AnswerService;
import com.ccat.api.service.QuestionService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Question bank management")
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService   answerService;

    // -------------------------------------------------------------------------
    // Questions
    // -------------------------------------------------------------------------

    @Operation(summary = "List all active questions (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questions found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuestionResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuestionResponse>> findAll() {
        return ResponseEntity.ok(questionService.findAll());
    }

    @Operation(summary = "List questions by domain")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questions found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuestionResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Domain not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/domain/{domainCode}")
    public ResponseEntity<List<QuestionResponse>> findByDomain(@PathVariable String domainCode) {
        return ResponseEntity.ok(questionService.findByDomain(domainCode));
    }

    @Operation(summary = "List questions by domain and difficulty")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questions found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuestionResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Domain not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/domain/{domainCode}/difficulty/{difficulty}")
    public ResponseEntity<List<QuestionResponse>> findByDomainAndDifficulty(
            @PathVariable String domainCode,
            @PathVariable String difficulty) {
        return ResponseEntity.ok(questionService.findByDomainAndDifficulty(domainCode, difficulty));
    }

    @Operation(summary = "Get question by UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question found",
                    content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Question not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<QuestionResponse> findByUuid(@PathVariable String uuid) {
        return ResponseEntity.ok(questionService.findByUuid(uuid));
    }

    @Operation(summary = "Create question (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Question created",
                    content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> create(
            @Valid @RequestBody QuestionCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.create(request, principal.getUsername()));
    }

    @Operation(summary = "Update question (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question updated",
                    content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Question not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> update(
            @PathVariable String uuid,
            @RequestBody QuestionUpdateRequest request) {
        return ResponseEntity.ok(questionService.update(uuid, request));
    }

    @Operation(summary = "Deactivate question (admin)", description = "Soft delete — sets bActive to false.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Question deactivated"),
            @ApiResponse(responseCode = "404", description = "Question not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String uuid) {
        questionService.delete(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Bulk import questions with answers (admin)",
               description = "Import multiple questions at once. Each question must include its answers.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Questions imported",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuestionResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/bulk-import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuestionResponse>> bulkImport(
            @Valid @RequestBody List<QuestionImportRequest> requests,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.bulkImport(requests, principal.getUsername()));
    }

    // -------------------------------------------------------------------------
    // Answers (nested under question)
    // -------------------------------------------------------------------------

    @Operation(summary = "List answers for a question")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Answers found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AnswerResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Question not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{questionUuid}/answers")
    public ResponseEntity<List<AnswerResponse>> findAnswers(@PathVariable String questionUuid) {
        return ResponseEntity.ok(answerService.findByQuestion(questionUuid));
    }

    @Operation(summary = "Add answer to a question (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Answer created",
                    content = @Content(schema = @Schema(implementation = AnswerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{questionUuid}/answers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerResponse> addAnswer(
            @Valid @RequestBody AnswerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(answerService.create(request));
    }

    @Operation(summary = "Update answer (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Answer updated",
                    content = @Content(schema = @Schema(implementation = AnswerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Answer not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/answers/{answerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerResponse> updateAnswer(
            @PathVariable Long answerId,
            @RequestBody AnswerCreateRequest request) {
        return ResponseEntity.ok(answerService.update(answerId, request));
    }

    @Operation(summary = "Delete answer (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Answer deleted"),
            @ApiResponse(responseCode = "404", description = "Answer not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/answers/{answerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long answerId) {
        answerService.delete(answerId);
        return ResponseEntity.noContent().build();
    }
}
