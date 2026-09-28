package com.lanedesk.shipment;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByStatusOrderByPickupAt(ShipmentStatus status);

    List<Shipment> findByShipperIdOrderByQuotedAtDesc(Long shipperId);

    List<Shipment> findByStatusInAndPickupAtBetween(Collection<ShipmentStatus> statuses, Instant from, Instant to);

    long countByQuotedAtBetween(Instant from, Instant to);
}
