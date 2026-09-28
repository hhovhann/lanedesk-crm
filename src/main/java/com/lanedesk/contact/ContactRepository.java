package com.lanedesk.contact;

import com.lanedesk.company.CompanyStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByCompanyIdOrderByName(Long companyId);

    List<Contact> findByPhoneIsNotNullAndCompanyStatusNotIn(List<CompanyStatus> excluded);
}
