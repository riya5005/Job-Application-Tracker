package com.atstracker.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class JobDtos {

    public static class JobRequest {
        @NotBlank
        public String title;
        @NotBlank
        public String location;
        public String skillsRequired;
        public String description;
    }

    // flattening this out instead of returning the raw entity so we don't
    // accidentally leak the recruiter's password hash etc through postedBy
    public static class JobResponse {
        public Long id;
        public String title;
        public String location;
        public String skillsRequired;
        public String description;
        public String postedByName;
        public Long postedById;
        public boolean active;
        public LocalDateTime createdAt;
        public long applicantCount; // only populated on recruiter's own-jobs endpoint
    }

    // keeping this deliberately simple rather than exposing Spring's full Page<> shape -
    // the frontend just needs to know what it got back and whether there's more
    public static class PagedJobResponse {
        public java.util.List<JobResponse> jobs;
        public int page;
        public int totalPages;
        public long totalResults;

        public PagedJobResponse(java.util.List<JobResponse> jobs, int page, int totalPages, long totalResults) {
            this.jobs = jobs;
            this.page = page;
            this.totalPages = totalPages;
            this.totalResults = totalResults;
        }
    }
}
