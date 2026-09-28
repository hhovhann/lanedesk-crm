package com.lanedesk.company;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CompanyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CompanyStatus status = CompanyStatus.PROSPECT;

    @Column(name = "mc_number")
    public String mcNumber;

    public String city;
    public String state;

    @Enumerated(EnumType.STRING)
    public Equipment equipment;

    public String website;
    public String notes;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    public Instant createdAt;

    public boolean isDoNotCall() {
        return status == CompanyStatus.DO_NOT_CALL;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public CompanyType getType() { return type; }
    public CompanyStatus getStatus() { return status; }
    public String getMcNumber() { return mcNumber; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public Equipment getEquipment() { return equipment; }
    public String getWebsite() { return website; }
    public String getNotes() { return notes; }
}
