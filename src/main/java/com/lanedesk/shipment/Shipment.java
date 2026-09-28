package com.lanedesk.shipment;

import com.lanedesk.company.Company;
import com.lanedesk.company.Equipment;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shipment")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "shipper_id")
    public Company shipper;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "carrier_id")
    public Company carrier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ShipmentStatus status = ShipmentStatus.QUOTED;

    @Column(name = "origin_city", nullable = false)
    public String originCity;
    @Column(name = "origin_state", nullable = false)
    public String originState;
    @Column(name = "dest_city", nullable = false)
    public String destCity;
    @Column(name = "dest_state", nullable = false)
    public String destState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Equipment equipment;

    @Column(name = "weight_lbs", nullable = false)
    public int weightLbs;

    @Column(name = "pickup_at", nullable = false)
    public Instant pickupAt;
    @Column(name = "delivery_at", nullable = false)
    public Instant deliveryAt;

    @Column(name = "customer_rate", nullable = false)
    public BigDecimal customerRate;
    @Column(name = "carrier_cost")
    public BigDecimal carrierCost;

    @Column(insertable = false, updatable = false)
    public BigDecimal margin;

    @Column(name = "lost_reason")
    public String lostReason;

    @Column(name = "quoted_at", nullable = false)
    public Instant quotedAt = Instant.now();

    public Long getId() { return id; }
    public Company getShipper() { return shipper; }
    public Company getCarrier() { return carrier; }
    public ShipmentStatus getStatus() { return status; }
    public String getOriginCity() { return originCity; }
    public String getOriginState() { return originState; }
    public String getDestCity() { return destCity; }
    public String getDestState() { return destState; }
    public Equipment getEquipment() { return equipment; }
    public int getWeightLbs() { return weightLbs; }
    public Instant getPickupAt() { return pickupAt; }
    public Instant getDeliveryAt() { return deliveryAt; }
    public BigDecimal getCustomerRate() { return customerRate; }
    public BigDecimal getCarrierCost() { return carrierCost; }
    public BigDecimal getMargin() { return margin; }
    public String getLostReason() { return lostReason; }
    public Instant getQuotedAt() { return quotedAt; }

    public String getLane() {
        return originCity + ", " + originState + " → " + destCity + ", " + destState;
    }
}
