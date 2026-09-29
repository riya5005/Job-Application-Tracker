package com.atstracker.dto;

import java.util.Map;

public class AnalyticsDtos {

    public static class RecruiterSummary {
        public long totalJobs;
        public long activeJobs;
        public long totalApplicants;
        public Map<String, Long> statusBreakdown; // e.g. { "APPLIED": 12, "SHORTLISTED": 4, ... }
        public Map<String, Long> applicantsPerJob; // job title -> applicant count, for the top jobs
        public double averageRating; // across every rated application, 0 if none rated yet
    }
}
