package com.ccat.api.controller;

import com.ccat.api.dto.response.UserResponse;
import com.ccat.api.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "Admin user management")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "List all platform users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(adminUserService.findAllUsers());
    }

    @Operation(summary = "Activate a user account", description = "Restores a suspended account back to ACTIVE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User activated",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid action",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<UserResponse> activate(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(adminUserService.activateUser(userId, principal.getUsername()));
    }

    @Operation(summary = "Suspend a user account", description = "Temporarily blocks a user from signing in.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User suspended",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid action",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<UserResponse> suspend(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(adminUserService.suspendUser(userId, principal.getUsername()));
    }

    @Operation(summary = "Ban a user account", description = "Marks the account as DELETED and blocks any future access.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User banned",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid action",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{userId}/ban")
    public ResponseEntity<UserResponse> ban(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(adminUserService.banUser(userId, principal.getUsername()));
    }
}
