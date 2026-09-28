package com.lanedesk.contact;

import com.lanedesk.company.Company;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "contact")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "company_id")
    public Company company;

    @Column(nullable = false)
    public String name;
    public String title;
    public String phone;
    public String email;

    @Column(name = "time_zone", nullable = false)
    public String timeZone = "America/Chicago";

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    public Instant createdAt;

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getTimeZone() { return timeZone; }
}
