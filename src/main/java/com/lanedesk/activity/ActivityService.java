package com.lanedesk.activity;

import com.lanedesk.company.Company;
import com.lanedesk.company.CompanyRepository;
import com.lanedesk.company.CompanyStatus;
import com.lanedesk.config.LaneDeskProperties;
import com.lanedesk.contact.Contact;
import com.lanedesk.contact.ContactRepository;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {

    private final ActivityRepository activities;
    private final CompanyRepository companies;
    private final ContactRepository contacts;
    private final LaneDeskProperties props;

    public ActivityService(ActivityRepository activities, CompanyRepository companies,
                           ContactRepository contacts, LaneDeskProperties props) {
        this.activities = activities;
        this.companies = companies;
        this.contacts = contacts;
        this.props = props;
    }

    /** Logs a call, closes any open follow-ups for the company and optionally schedules the next one. */
    @Transactional
    public Activity logCall(Long companyId, Long contactId, Outcome outcome, String notes, Integer followUpDays) {
        Company company = companies.findById(companyId).orElseThrow();
        if (company.isDoNotCall()) {
            throw new IllegalStateException(company.name + " is marked Do Not Call");
        }
        Contact contact = contactId == null ? null : contacts.findById(contactId).orElseThrow();
        activities.completeFollowUps(companyId);

        Activity a = new Activity();
        a.company = company;
        a.contact = contact;
        a.type = ActivityType.CALL;
        a.outcome = outcome;
        a.notes = notes == null || notes.isBlank() ? null : notes.trim();
        a.occurredAt = Instant.now();
        if (outcome == Outcome.NOT_INTERESTED) {
            company.status = CompanyStatus.INACTIVE;
        } else if (followUpDays != null && followUpDays > 0) {
            a.nextFollowUpAt = followUpAt(followUpDays);
        }
        if (company.status == CompanyStatus.PROSPECT) {
            company.status = CompanyStatus.CONTACTED;
        }
        if (outcome == Outcome.CONVERSATION && company.status == CompanyStatus.CONTACTED) {
            company.status = CompanyStatus.QUALIFIED;
        }
        return activities.save(a);
    }

    @Transactional
    public Activity addNote(Long companyId, String notes) {
        Activity a = new Activity();
        a.company = companies.findById(companyId).orElseThrow();
        a.type = ActivityType.NOTE;
        a.notes = notes;
        return activities.save(a);
    }

    /** Follow-ups land at 09:00 in the agent's zone, N days out. */
    private Instant followUpAt(int days) {
        ZonedDateTime t = ZonedDateTime.now(props.agentTimeZone()).plusDays(days).with(LocalTime.of(9, 0));
        return t.toInstant();
    }
}
