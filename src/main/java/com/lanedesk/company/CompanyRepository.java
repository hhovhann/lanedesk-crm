package com.lanedesk.company;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("""
            select c from Company c
            where (:type is null or c.type = :type)
              and (:status is null or c.status = :status)
              and (:state is null or c.state = :state)
              and (:equipment is null or c.equipment = :equipment)
              and (:q is null or lower(c.name) like lower(concat('%', cast(:q as string), '%')))
            order by c.name
            """)
    List<Company> search(@Param("type") CompanyType type, @Param("status") CompanyStatus status,
                         @Param("state") String state, @Param("equipment") Equipment equipment,
                         @Param("q") String q);

    List<Company> findByTypeAndStatusNotOrderByName(CompanyType type, CompanyStatus status);

    boolean existsByMcNumber(String mcNumber);

    Optional<Company> findByNameIgnoreCaseAndState(String name, String state);
}
