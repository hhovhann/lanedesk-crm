package com.lanedesk.shipment;

import com.lanedesk.company.Company;
import com.lanedesk.company.CompanyRepository;
import com.lanedesk.company.CompanyStatus;
import com.lanedesk.company.CompanyType;
import com.lanedesk.company.Equipment;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentService {

    private final ShipmentRepository shipments;
    private final CompanyRepository companies;

    public ShipmentService(ShipmentRepository shipments, CompanyRepository companies) {
        this.shipments = shipments;
        this.companies = companies;
    }

    @Transactional
    public Shipment quote(Long shipperId, String originCity, String originState, String destCity, String destState,
                          Equipment equipment, int weightLbs, Instant pickupAt, Instant deliveryAt, BigDecimal customerRate) {
        if (weightLbs <= 0 || weightLbs > 48000) {
            throw new IllegalArgumentException("Weight must be between 1 and 48,000 lbs");
        }
        if (deliveryAt.isBefore(pickupAt)) {
            throw new IllegalArgumentException("Delivery cannot be before pickup");
        }
        Shipment s = new Shipment();
        s.shipper = companies.findById(shipperId).orElseThrow();
        s.originCity = originCity.trim();
        s.originState = originState.trim().toUpperCase();
        s.destCity = destCity.trim();
        s.destState = destState.trim().toUpperCase();
        s.equipment = equipment;
        s.weightLbs = weightLbs;
        s.pickupAt = pickupAt;
        s.deliveryAt = deliveryAt;
        s.customerRate = customerRate;
        Shipment saved = shipments.save(s);
        if (s.shipper.status == CompanyStatus.PROSPECT || s.shipper.status == CompanyStatus.CONTACTED
                || s.shipper.status == CompanyStatus.QUALIFIED) {
            s.shipper.status = CompanyStatus.QUALIFIED;
        }
        return saved;
    }

    /** Moves one step along QUOTED → WON → COVERED → IN_TRANSIT → DELIVERED, enforcing what each step needs. */
    @Transactional
    public Shipment advance(Long id, Long carrierId, BigDecimal carrierCost) {
        Shipment s = shipments.findById(id).orElseThrow();
        ShipmentStatus next = s.status.next();
        if (next == null) {
            throw new IllegalStateException("Load is already " + s.status);
        }
        if (next == ShipmentStatus.COVERED) {
            if (carrierId == null || carrierCost == null) {
                throw new IllegalArgumentException("Pick a carrier and enter the carrier cost to cover this load");
            }
            Company carrier = companies.findById(carrierId).orElseThrow();
            if (carrier.type != CompanyType.CARRIER) {
                throw new IllegalArgumentException(carrier.name + " is not a carrier");
            }
            s.carrier = carrier;
            s.carrierCost = carrierCost;
        }
        if (next == ShipmentStatus.WON && s.shipper.status != CompanyStatus.ACTIVE) {
            s.shipper.status = CompanyStatus.ACTIVE;
        }
        s.status = next;
        return s;
    }

    @Transactional
    public Shipment lose(Long id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Enter a reason the load was lost");
        }
        Shipment s = shipments.findById(id).orElseThrow();
        if (s.status != ShipmentStatus.QUOTED && s.status != ShipmentStatus.WON) {
            throw new IllegalStateException("Only quoted or won loads can be marked lost");
        }
        s.status = ShipmentStatus.LOST;
        s.lostReason = reason.trim();
        return s;
    }
}
