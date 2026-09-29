package com.atstracker.dto;

import com.atstracker.model.ApplicationStatus;

import java.time.LocalDateTime;

public class ApplicationDtos {

    public static class ApplyRequest {
        public String resumeLink; // just a link for now (drive/dropbox etc), not handling file uploads
    }

    public static class StatusUpdateRequest {
        public ApplicationStatus status;
        public String recruiterNotes;
        public Integer rating; // 1-5, optional - only sent when the recruiter is rating this candidate
    }

    public static class ApplicationResponse {
        public Long id;
        public Long jobId;
        public String jobTitle;
        public Long candidateId;
        public String candidateName;
        public String candidateEmail;
        public ApplicationStatus status;
        public String recruiterNotes;
        public Integer rating;
        public String resumeLink;
        public String resumeFileName;   // original filename, for display
        public boolean hasResumeFile;   // whether there's an uploaded file to download
        public LocalDateTime appliedAt;
        public LocalDateTime updatedAt;
    }
}
