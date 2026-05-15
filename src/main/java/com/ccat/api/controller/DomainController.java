package com.ccat.api.controller;

import com.ccat.api.dto.request.DomainCreateRequest;
import com.ccat.api.dto.request.DomainUpdateRequest;
import com.ccat.api.dto.response.DomainResponse;
import com.ccat.api.service.DomainService;
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
@RequestMapping("/api/v1/domains")
@RequiredArgsConstructor
@Tag(name = "Domains", description = "Question bank domains (categories)")
public class DomainController {

    private final DomainService domainService;

    @Operation(summary = "List all active domains")
    @ApiResponse(responseCode = "200", description = "Active domains",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DomainResponse.class))))
    @GetMapping
    public ResponseEntity<List<DomainResponse>> findAllActive() {
        return ResponseEntity.ok(domainService.findAllActive());
    }

    @Operation(summary = "List all domains including inactive (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All domains",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DomainResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DomainResponse>> findAll() {
        return ResponseEntity.ok(domainService.findAll());
    }

    @Operation(summary = "Get domain by code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Domain found",
                    content = @Content(schema = @Schema(implementation = DomainResponse.class))),
            @ApiResponse(responseCode = "404", description = "Domain not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{domainCode}")
    public ResponseEntity<DomainResponse> findByCode(@PathVariable String domainCode) {
        return ResponseEntity.ok(domainService.findByCode(domainCode));
    }

    @Operation(summary = "Create domain (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Domain created",
                    content = @Content(schema = @Schema(implementation = DomainResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DomainResponse> create(
            @Valid @RequestBody DomainCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(domainService.create(request, principal.getUsername()));
    }

    @Operation(summary = "Update domain (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Domain updated",
                    content = @Content(schema = @Schema(implementation = DomainResponse.class))),
            @ApiResponse(responseCode = "404", description = "Domain not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{domainCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DomainResponse> update(
            @PathVariable String domainCode,
            @RequestBody DomainUpdateRequest request) {
        return ResponseEntity.ok(domainService.update(domainCode, request));
    }

    @Operation(summary = "Deactivate domain (admin)", description = "Soft delete — sets status to INACTIVE.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Domain deactivated"),
            @ApiResponse(responseCode = "404", description = "Domain not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{domainCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String domainCode) {
        domainService.delete(domainCode);
        return ResponseEntity.noContent().build();
    }
}
