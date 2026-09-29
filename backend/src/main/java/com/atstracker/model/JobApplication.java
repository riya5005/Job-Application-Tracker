package com.atstracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_id", "candidate_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(length = 2000)
    private String recruiterNotes;

    private String resumeLink; // optional - candidate can paste a drive/portfolio link instead of/alongside uploading

    // the actual uploaded file, if they used the upload button instead of a link.
    // storedFileName is the name on disk (randomized so we don't get collisions),
    // originalFileName is what we show back to the recruiter in the UI.
    private String storedFileName;
    private String originalFileName;

    @Min(1)
    @Max(5)
    private Integer rating; // recruiter's own rating of the candidate, 1-5, nullable until they set one

    private LocalDateTime appliedAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
