package com.atstracker.repository;

import com.atstracker.model.Job;
import com.atstracker.model.JobApplication;
import com.atstracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByCandidate(User candidate);
    List<JobApplication> findByJob(Job job);
    List<JobApplication> findByJobPostedBy(User recruiter);
    Optional<JobApplication> findByJobAndCandidate(Job job, User candidate);
}
