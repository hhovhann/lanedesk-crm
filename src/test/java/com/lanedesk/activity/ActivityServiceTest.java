package com.lanedesk.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lanedesk.AbstractIntegrationTest;
import com.lanedesk.company.Company;
import com.lanedesk.company.CompanyRepository;
import com.lanedesk.company.CompanyStatus;
import com.lanedesk.company.CompanyType;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class ActivityServiceTest extends AbstractIntegrationTest {

    @Autowired ActivityService service;
    @Autowired ActivityRepository activities;
    @Autowired CompanyRepository companies;
    @Autowired JdbcTemplate jdbc;

    private Company company(String name, CompanyStatus status) {
        Company c = new Company();
        c.name = name;
        c.type = CompanyType.SHIPPER;
        c.status = status;
        return companies.save(c);
    }

    @Test
    void loggingCallSchedulesFollowUpAndClosesOlderOnes() {
        Company c = company("Call Co", CompanyStatus.PROSPECT);
        Activity first = service.logCall(c.id, null, Outcome.NO_ANSWER, null, 2);
        Activity second = service.logCall(c.id, null, Outcome.CONVERSATION, "  talked  ", 7);
        Boolean done = jdbc.queryForObject("select follow_up_done from activity where id = ?", Boolean.class, first.id);
        assertThat(done).isTrue();
        assertThat(second.notes).isEqualTo("talked");
        assertThat(second.nextFollowUpAt).isAfter(Instant.now().plusSeconds(6 * 86400));
        assertThat(companies.findById(c.id).orElseThrow().status).isEqualTo(CompanyStatus.QUALIFIED);
    }

    @Test
    void doNotCallCompaniesCannotBeCalled() {
        Company c = company("DNC Co", CompanyStatus.DO_NOT_CALL);
        assertThatThrownBy(() -> service.logCall(c.id, null, Outcome.NO_ANSWER, null, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void notInterestedMakesCompanyInactiveWithoutFollowUp() {
        Company c = company("Nope Co", CompanyStatus.CONTACTED);
        Activity a = service.logCall(c.id, null, Outcome.NOT_INTERESTED, null, 7);
        assertThat(a.nextFollowUpAt).isNull();
        assertThat(companies.findById(c.id).orElseThrow().status).isEqualTo(CompanyStatus.INACTIVE);
    }
}
