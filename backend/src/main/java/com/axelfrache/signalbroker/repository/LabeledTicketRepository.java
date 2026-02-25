package com.axelfrache.signalbroker.repository;

import com.axelfrache.signalbroker.model.entity.LabeledTicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LabeledTicketRepository extends JpaRepository<LabeledTicketEntity, String> {

        @Query("SELECT t FROM LabeledTicketEntity t " +
                        "WHERE (:priorities IS NULL OR t.priority IN :priorities) " +
                        "AND (:categories IS NULL OR t.category IN :categories) " +
                        "AND (:q IS NULL OR LOWER(t.subject) LIKE LOWER(:qPattern) " +
                        "     OR LOWER(t.body) LIKE LOWER(:qPattern) " +
                        "     OR LOWER(t.contact) LIKE LOWER(:qPattern)) " +
                        "AND (:fromTime IS NULL OR t.receivedAt >= :fromTime) " +
                        "AND (:toTime IS NULL OR t.receivedAt <= :toTime)")
        Page<LabeledTicketEntity> findWithFilters(
                        @Param("priorities") List<String> priorities,
                        @Param("categories") List<String> categories,
                        @Param("q") String q,
                        @Param("qPattern") String qPattern,
                        @Param("fromTime") Double fromTime,
                        @Param("toTime") Double toTime,
                        Pageable pageable);

        @Query("SELECT COUNT(t) FROM LabeledTicketEntity t WHERE t.receivedAt >= :fromTime AND t.receivedAt <= :toTime")
        long countByTimeRange(@Param("fromTime") Double fromTime, @Param("toTime") Double toTime);

        @Query("SELECT MAX(t.receivedAt) FROM LabeledTicketEntity t")
        Double findMaxReceivedAt();

        @Query("SELECT MIN(t.receivedAt) FROM LabeledTicketEntity t")
        Double findMinReceivedAt();

        @Query("SELECT t.priority, COUNT(t) FROM LabeledTicketEntity t WHERE t.receivedAt >= :fromTime AND t.receivedAt <= :toTime GROUP BY t.priority")
        List<Object[]> countByPriorityInTimeRange(@Param("fromTime") Double fromTime, @Param("toTime") Double toTime);

        @Query("SELECT t.category, COUNT(t) FROM LabeledTicketEntity t WHERE t.receivedAt >= :fromTime AND t.receivedAt <= :toTime GROUP BY t.category")
        List<Object[]> countByCategoryInTimeRange(@Param("fromTime") Double fromTime, @Param("toTime") Double toTime);

        @Query("SELECT AVG(t.confidence) FROM LabeledTicketEntity t WHERE t.receivedAt >= :fromTime AND t.receivedAt <= :toTime")
        Double getAverageConfidenceInTimeRange(@Param("fromTime") Double fromTime, @Param("toTime") Double toTime);

        @Query(value = "SELECT FLOOR(\"receivedAt\" / :bucketSize) * :bucketSize AS bucket, COUNT(*) " +
                        "FROM labeled_tickets WHERE \"receivedAt\" >= :fromTime AND \"receivedAt\" <= :toTime " +
                        "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
        List<Object[]> getTimeSeriesCounts(@Param("fromTime") Double fromTime, @Param("toTime") Double toTime,
                        @Param("bucketSize") Double bucketSize);

}
