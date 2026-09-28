package com.lanedesk.today;

import static org.assertj.core.api.Assertions.assertThat;

import com.lanedesk.AbstractIntegrationTest;
import com.lanedesk.company.Company;
import com.lanedesk.company.CompanyRepository;
import com.lanedesk.company.CompanyStatus;
import com.lanedesk.company.CompanyType;
import com.lanedesk.contact.Contact;
import com.lanedesk.contact.ContactRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TodayServiceTest extends AbstractIntegrationTest {

    @Autowired TodayService today;
    @Autowired CompanyRepository companies;
    @Autowired ContactRepository contacts;

    private Contact contact(String zone) {
        Contact c = new Contact();
        c.timeZone = zone;
        c.name = "X";
        c.phone = "555";
        return c;
    }

    @Test
    void businessHoursAreEvaluatedInTheContactsZone() {
        // Tuesday 2026-09-29 15:00 UTC = 11:00 New York, 08:00 Los Angeles, 05:00 Honolulu
        Instant t = ZonedDateTime.of(2026, 9, 29, 15, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        assertThat(TodayService.isCallable(contact("America/New_York"), t)).isTrue();
        assertThat(TodayService.isCallable(contact("America/Los_Angeles"), t)).isTrue();
        assertThat(TodayService.isCallable(contact("Pacific/Honolulu"), t)).isFalse();
        // 17:00 local is closed
        Instant closing = ZonedDateTime.of(2026, 9, 29, 21, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        assertThat(TodayService.isCallable(contact("America/New_York"), closing)).isFalse();
    }

    @Test
    void weekendsAreNeverCallable() {
        Instant saturdayNoon = ZonedDateTime.of(2026, 10, 3, 17, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        assertThat(TodayService.isCallable(contact("America/New_York"), saturdayNoon)).isFalse();
    }

    @Test
    void doNotCallCompaniesNeverAppearOnCallList() {
        Company c = new Company();
        c.name = "Never Call";
        c.type = CompanyType.SHIPPER;
        c.status = CompanyStatus.DO_NOT_CALL;
        companies.save(c);
        Contact ct = contact("America/New_York");
        ct.company = c;
        contacts.save(ct);
        assertThat(today.build().callList()).noneMatch(t -> t.contact().company.id.equals(c.id));
    }
}
