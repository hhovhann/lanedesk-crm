package com.lanedesk.activity;

import com.lanedesk.company.Company;
import com.lanedesk.contact.Contact;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "company_id")
    public Company company;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_id")
    public Contact contact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ActivityType type;

    @Enumerated(EnumType.STRING)
    public Outcome outcome;

    public String notes;

    @Column(name = "occurred_at", nullable = false)
    public Instant occurredAt = Instant.now();

    @Column(name = "next_follow_up_at")
    public Instant nextFollowUpAt;

    @Column(name = "follow_up_done", nullable = false)
    public boolean followUpDone;

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public Contact getContact() { return contact; }
    public ActivityType getType() { return type; }
    public Outcome getOutcome() { return outcome; }
    public String getNotes() { return notes; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getNextFollowUpAt() { return nextFollowUpAt; }
    public boolean isFollowUpDone() { return followUpDone; }
}
