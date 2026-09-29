package com.atstracker.controller;

import com.atstracker.dto.JobDtos.*;
import com.atstracker.model.Job;
import com.atstracker.model.Role;
import com.atstracker.model.User;
import com.atstracker.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final CurrentUser currentUser;

    public JobController(JobService jobService, CurrentUser currentUser) {
        this.jobService = jobService;
        this.currentUser = currentUser;
    }

    // public listing, anyone can browse without logging in. Paginated so this doesn't
    // fall over once there are a couple hundred jobs in the table instead of a dozen.
    @GetMapping
    public PagedJobResponse listJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        Page<Job> result = jobService.searchPaged(search, location, page, size);
        List<JobResponse> jobResponses = result.getContent().stream()
                .map(jobService::toResponse)
                .collect(Collectors.toList());

        return new PagedJobResponse(jobResponses, result.getNumber(), result.getTotalPages(), result.getTotalElements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJob(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(jobService.toResponse(jobService.getById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createJob(@Valid @RequestBody JobRequest req, Authentication auth) {
        User user = currentUser.get(auth);
        if (user.getRole() != Role.RECRUITER) {
            return ResponseEntity.status(403).body("Only recruiters can post jobs");
        }
        Job job = jobService.createJob(req, user);
        return ResponseEntity.ok(jobService.toResponse(job));
    }

    // jobs posted by the logged-in recruiter - used for their dashboard
    @GetMapping("/mine")
    public ResponseEntity<?> myJobs(Authentication auth) {
        User user = currentUser.get(auth);
        if (user.getRole() != Role.RECRUITER) {
            return ResponseEntity.status(403).body("Only recruiters have job postings");
        }
        List<JobResponse> jobs = jobService.getJobsPostedBy(user).stream()
                .map(jobService::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(jobs);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<?> closeJob(@PathVariable Long id, Authentication auth) {
        User user = currentUser.get(auth);
        try {
            jobService.closeJob(id, user);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
