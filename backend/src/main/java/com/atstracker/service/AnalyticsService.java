package com.atstracker.service;

import com.atstracker.dto.AnalyticsDtos.RecruiterSummary;
import com.atstracker.model.ApplicationStatus;
import com.atstracker.model.Job;
import com.atstracker.model.JobApplication;
import com.atstracker.model.User;
import com.atstracker.repository.ApplicationRepository;
import com.atstracker.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public AnalyticsService(JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public RecruiterSummary buildSummary(User recruiter) {
        List<Job> jobs = jobRepository.findByPostedBy(recruiter);
        List<JobApplication> applications = applicationRepository.findByJobPostedBy(recruiter);

        RecruiterSummary summary = new RecruiterSummary();
        summary.totalJobs = jobs.size();
        summary.activeJobs = jobs.stream().filter(Job::isActive).count();
        summary.totalApplicants = applications.size();

        // pre-seed every status at 0 so the frontend chart doesn't have to guess
        // which keys might be missing - always get the full funnel back
        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusBreakdown.put(status.name(), 0L);
        }
        for (JobApplication app : applications) {
            statusBreakdown.merge(app.getStatus().name(), 1L, Long::sum);
        }
        summary.statusBreakdown = statusBreakdown;

        summary.applicantsPerJob = applications.stream()
                .collect(Collectors.groupingBy(a -> a.getJob().getTitle(), LinkedHashMap::new, Collectors.counting()));

        summary.averageRating = applications.stream()
                .map(JobApplication::getRating)
                .filter(r -> r != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return summary;
    }
}
