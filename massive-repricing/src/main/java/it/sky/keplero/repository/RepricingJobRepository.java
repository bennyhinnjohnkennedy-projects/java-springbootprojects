package it.sky.keplero.repository;

import it.sky.keplero.entity.RepricingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RepricingJobRepository extends JpaRepository<RepricingJob, Integer> {
    @Query("UPDATE RepricingJob r SET r.processed = :processed, r.succeeded = :succeeded, r.failed = :failed, r.jobStatus = :jobStatus WHERE r.id = :id")
    @Modifying
    void updateJobDetails(int id, int processed, int succeeded, int failed, String jobStatus);

    @Query("UPDATE RepricingJob r SET r.jobStatus = :jobStatus, r.endTime = :endTime where r.id = :id")
    @Modifying
    void updateEndJobStatus(int id, String jobStatus, Instant endTime);

    @Query("UPDATE RepricingJob r SET r.jobStatus = :jobStatus, r.startTime = :startTime where r.id = :id")
    @Modifying
    void updateStartJobStatus(int id, String jobStatus, Instant startTime);

    @Query("SELECT rj.id, rj.inputFileName FROM RepricingJob rj WHERE rj.jobStatus = :status ORDER BY rj.startTime")
    List<Object[]> findByJobStatusOrderByStartTime(String status);
}
