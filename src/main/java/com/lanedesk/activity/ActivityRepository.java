package com.lanedesk.activity;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByCompanyIdOrderByOccurredAtDesc(Long companyId);

    @Query("""
            select a from Activity a
            where a.nextFollowUpAt is not null and a.followUpDone = false
              and a.nextFollowUpAt <= :until and a.company.status <> 'DO_NOT_CALL'
            order by a.nextFollowUpAt
            """)
    List<Activity> dueFollowUps(@Param("until") Instant until);

    @Query("select a.company.id from Activity a where a.type = 'CALL' and a.occurredAt >= :since")
    List<Long> companyIdsCalledSince(@Param("since") Instant since);

    @Query("select a from Activity a where a.type = 'CALL' and a.occurredAt >= :from and a.occurredAt < :to")
    List<Activity> callsBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Modifying
    @Query("update Activity a set a.followUpDone = true where a.company.id = :companyId and a.followUpDone = false and a.nextFollowUpAt is not null")
    void completeFollowUps(@Param("companyId") Long companyId);
}
