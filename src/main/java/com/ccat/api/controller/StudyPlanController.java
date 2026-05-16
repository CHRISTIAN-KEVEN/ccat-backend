package com.ccat.api.controller;

import com.ccat.api.dto.response.StudyPlanResponse;
import com.ccat.api.service.StudyPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/study")
@RequiredArgsConstructor
@Tag(name = "Study Plan", description = "Personalized daily study plan and drill tracking")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @Operation(summary = "Get the current user's study plan (today + week + milestones + domain progress)")
    @GetMapping("/plan")
    public ResponseEntity<StudyPlanResponse> getPlan(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(studyPlanService.getPlan(principal.getUsername()));
    }

    @Operation(summary = "Toggle a drill as done/undone")
    @PatchMapping("/drills/{drillKey}/complete")
    public ResponseEntity<Void> completeDrill(
            @PathVariable String drillKey,
            @AuthenticationPrincipal UserDetails principal) {
        studyPlanService.completeDrill(drillKey, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
