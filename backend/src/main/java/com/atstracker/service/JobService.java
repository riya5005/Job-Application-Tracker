package com.atstracker.service;

import com.atstracker.dto.JobDtos.*;
import com.atstracker.model.Job;
import com.atstracker.model.User;
import com.atstracker.repository.ApplicationRepository;
import com.atstracker.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public Job createJob(JobRequest req, User recruiter) {
        Job job = new Job();
        job.setTitle(req.title);
        job.setLocation(req.location);
        job.setSkillsRequired(req.skillsRequired);
        job.setDescription(req.description);
        job.setPostedBy(recruiter);
        return jobRepository.save(job);
    }

    public List<Job> getActiveJobs() {
        return jobRepository.findByActiveTrue();
    }

    public List<Job> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return getActiveJobs();
        // keeping the old simple version around too since a couple of other things assumed a List
        return jobRepository.search(keyword, "", Pageable.unpaged()).getContent();
    }

    // the version the paginated listing endpoint actually calls - page is 0-indexed
    public Page<Job> searchPaged(String keyword, String location, int page, int size) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim();
        String cleanLocation = (location == null) ? "" : location.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jobRepository.search(cleanKeyword, cleanLocation, pageable);
    }

    public List<Job> getJobsPostedBy(User recruiter) {
        return jobRepository.findByPostedBy(recruiter);
    }

    public Job getById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
    }

    public void closeJob(Long id, User recruiter) {
        Job job = getById(id);
        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            throw new SecurityException("You didn't post this job");
        }
        job.setActive(false);
        jobRepository.save(job);
    }

    public JobResponse toResponse(Job job) {
        JobResponse r = new JobResponse();
        r.id = job.getId();
        r.title = job.getTitle();
        r.location = job.getLocation();
        r.skillsRequired = job.getSkillsRequired();
        r.description = job.getDescription();
        r.postedByName = job.getPostedBy().getFullName();
        r.postedById = job.getPostedBy().getId();
        r.active = job.isActive();
        r.createdAt = job.getCreatedAt();
        r.applicantCount = applicationRepository.findByJob(job).size();
        return r;
    }
}
