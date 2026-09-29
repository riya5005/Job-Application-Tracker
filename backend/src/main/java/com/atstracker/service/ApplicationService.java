package com.atstracker.service;

import com.atstracker.dto.ApplicationDtos.*;
import com.atstracker.model.*;
import com.atstracker.repository.ApplicationRepository;
import com.atstracker.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final FileStorageService fileStorageService;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository,
                               FileStorageService fileStorageService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.fileStorageService = fileStorageService;
    }

    public JobApplication apply(Long jobId, User candidate, ApplyRequest req) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (!job.isActive()) {
            throw new IllegalStateException("This job isn't accepting applications anymore");
        }

        applicationRepository.findByJobAndCandidate(job, candidate).ifPresent(a -> {
            throw new IllegalStateException("Already applied to this job");
        });

        JobApplication app = new JobApplication();
        app.setJob(job);
        app.setCandidate(candidate);
        app.setResumeLink(req.resumeLink);
        return applicationRepository.save(app);
    }

    // candidate uploading (or replacing) their resume on an existing application
    public JobApplication uploadResume(Long applicationId, User candidate, MultipartFile file) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!app.getCandidate().getId().equals(candidate.getId())) {
            throw new SecurityException("Not your application");
        }

        String storedName = fileStorageService.store(file);
        app.setStoredFileName(storedName);
        app.setOriginalFileName(file.getOriginalFilename());
        return applicationRepository.save(app);
    }

    // used by both the owning candidate and the recruiter who posted the job -
    // access check happens in the controller since it needs to branch on who's asking
    public JobApplication getById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
    }

    public List<JobApplication> getMyApplications(User candidate) {
        return applicationRepository.findByCandidate(candidate);
    }

    public List<JobApplication> getApplicantsForJob(Long jobId, User recruiter) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            throw new SecurityException("Not your job posting");
        }
        return applicationRepository.findByJob(job);
    }

    public List<JobApplication> getAllApplicantsForRecruiter(User recruiter) {
        return applicationRepository.findByJobPostedBy(recruiter);
    }

    public JobApplication updateStatus(Long applicationId, User recruiter, StatusUpdateRequest req) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!app.getJob().getPostedBy().getId().equals(recruiter.getId())) {
            throw new SecurityException("Not your job posting");
        }

        if (req.status != null) app.setStatus(req.status);
        // notes/rating are optional on every update - only overwrite if something was actually sent
        if (req.recruiterNotes != null) app.setRecruiterNotes(req.recruiterNotes);
        if (req.rating != null) app.setRating(req.rating);
        app.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    public ApplicationResponse toResponse(JobApplication app) {
        ApplicationResponse r = new ApplicationResponse();
        r.id = app.getId();
        r.jobId = app.getJob().getId();
        r.jobTitle = app.getJob().getTitle();
        r.candidateId = app.getCandidate().getId();
        r.candidateName = app.getCandidate().getFullName();
        r.candidateEmail = app.getCandidate().getEmail();
        r.status = app.getStatus();
        r.recruiterNotes = app.getRecruiterNotes();
        r.rating = app.getRating();
        r.resumeLink = app.getResumeLink();
        r.resumeFileName = app.getOriginalFileName();
        r.hasResumeFile = app.getStoredFileName() != null;
        r.appliedAt = app.getAppliedAt();
        r.updatedAt = app.getUpdatedAt();
        return r;
    }
}
