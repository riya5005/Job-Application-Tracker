package com.atstracker.repository;

import com.atstracker.model.Job;
import com.atstracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByActiveTrue();
    List<Job> findByPostedBy(User recruiter);

    // keyword matches title/skills, location is separate. Pass "" (not null) to skip a filter -
    // postgres can't infer the type of a null param inside LOWER(), so we avoid nulls entirely.
    //
    // this is the one the paginated job listing actually uses
    @Query("""
        SELECT j FROM Job j WHERE j.active = true
        AND (:keyword = '' OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
        """)
    Page<Job> search(@Param("keyword") String keyword, @Param("location") String location, Pageable pageable);
}
