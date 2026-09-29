package com.atstracker.controller;

import com.atstracker.dto.ApplicationDtos.*;
import com.atstracker.model.JobApplication;
import com.atstracker.model.Role;
import com.atstracker.model.User;
import com.atstracker.service.ApplicationService;
import com.atstracker.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final CurrentUser currentUser;
    private final FileStorageService fileStorageService;

    public ApplicationController(ApplicationService applicationService, CurrentUser currentUser,
                                  FileStorageService fileStorageService) {
        this.applicationService = applicationService;
        this.currentUser = currentUser;
        this.fileStorageService = fileStorageService;
    }

    // candidate applies to a job
    @PostMapping("/jobs/{jobId}")
    public ResponseEntity<?> apply(@PathVariable Long jobId, @RequestBody(required = false) ApplyRequest req, Authentication auth) {
        User user = currentUser.get(auth);
        if (user.getRole() != Role.CANDIDATE) {
            return ResponseEntity.status(403).body("Only candidates can apply to jobs");
        }
        if (req == null) req = new ApplyRequest();

        try {
            JobApplication app = applicationService.apply(jobId, user, req);
            return ResponseEntity.ok(applicationService.toResponse(app));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage()); // already applied / job closed
        }
    }

    // candidate's own application history
    @GetMapping("/mine")
    public ResponseEntity<?> myApplications(Authentication auth) {
        User user = currentUser.get(auth);
        List<ApplicationResponse> apps = applicationService.getMyApplications(user).stream()
                .map(applicationService::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(apps);
    }

    // candidate uploads their resume file for an application they already submitted
    @PostMapping("/{id}/resume")
    public ResponseEntity<?> uploadResume(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication auth) {
        User user = currentUser.get(auth);
        try {
            JobApplication updated = applicationService.uploadResume(id, user, file);
            return ResponseEntity.ok(applicationService.toResponse(updated));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // download a resume - either the candidate who owns it, or the recruiter who owns the job
    @GetMapping("/{id}/resume")
    public ResponseEntity<?> downloadResume(@PathVariable Long id, Authentication auth) {
        User user = currentUser.get(auth);
        JobApplication app;
        try {
            app = applicationService.getById(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }

        boolean isOwner = app.getCandidate().getId().equals(user.getId());
        boolean isRecruiterForJob = app.getJob().getPostedBy().getId().equals(user.getId());
        if (!isOwner && !isRecruiterForJob) {
            return ResponseEntity.status(403).body("Not authorized to view this resume");
        }
        if (app.getStoredFileName() == null) {
            return ResponseEntity.status(404).body("No resume file uploaded for this application");
        }

        Resource resource = fileStorageService.load(app.getStoredFileName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + app.getOriginalFileName() + "\"")
                .body(resource);
    }

    // recruiter viewing applicants for one specific job
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<?> applicantsForJob(@PathVariable Long jobId, Authentication auth) {
        User user = currentUser.get(auth);
        try {
            List<ApplicationResponse> apps = applicationService.getApplicantsForJob(jobId, user).stream()
                    .map(applicationService::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(apps);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // recruiter dashboard - every applicant across all of their job postings
    @GetMapping("/recruiter/all")
    public ResponseEntity<?> allApplicantsForRecruiter(Authentication auth) {
        User user = currentUser.get(auth);
        if (user.getRole() != Role.RECRUITER) {
            return ResponseEntity.status(403).body("Recruiter access only");
        }
        List<ApplicationResponse> apps = applicationService.getAllApplicantsForRecruiter(user).stream()
                .map(applicationService::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(apps);
    }

    // move a candidate to the next stage (or reject etc)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req, Authentication auth) {
        User user = currentUser.get(auth);
        try {
            JobApplication updated = applicationService.updateStatus(id, user, req);
            return ResponseEntity.ok(applicationService.toResponse(updated));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
