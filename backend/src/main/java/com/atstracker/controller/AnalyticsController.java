package com.atstracker.controller;

import com.atstracker.model.Role;
import com.atstracker.model.User;
import com.atstracker.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUser currentUser;

    public AnalyticsController(AnalyticsService analyticsService, CurrentUser currentUser) {
        this.analyticsService = analyticsService;
        this.currentUser = currentUser;
    }

    @GetMapping("/recruiter")
    public ResponseEntity<?> recruiterSummary(Authentication auth) {
        User user = currentUser.get(auth);
        if (user.getRole() != Role.RECRUITER) {
            return ResponseEntity.status(403).body("Recruiter access only");
        }
        return ResponseEntity.ok(analyticsService.buildSummary(user));
    }
}
