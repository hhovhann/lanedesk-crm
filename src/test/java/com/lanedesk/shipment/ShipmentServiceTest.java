package com.lanedesk.shipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lanedesk.AbstractIntegrationTest;
import com.lanedesk.company.Company;
import com.lanedesk.company.CompanyRepository;
import com.lanedesk.company.CompanyType;
import com.lanedesk.company.Equipment;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class ShipmentServiceTest extends AbstractIntegrationTest {

    @Autowired ShipmentService service;
    @Autowired CompanyRepository companies;
    @Autowired JdbcTemplate jdbc;

    private Company company(String name, CompanyType type) {
        Company c = new Company();
        c.name = name;
        c.type = type;
        return companies.save(c);
    }

    private Shipment quote(Company shipper) {
        Instant pickup = Instant.now().plusSeconds(86400);
        return service.quote(shipper.id, "Dallas", "tx", "Denver", "co", Equipment.FLATBED, 40000,
                pickup, pickup.plusSeconds(86400), new BigDecimal("2000.00"));
    }

    @Test
    void fullLifecycleComputesMargin() {
        Company shipper = company("Ship Co", CompanyType.SHIPPER);
        Company carrier = company("Haul Co", CompanyType.CARRIER);
        Shipment s = quote(shipper);
        assertThat(s.originState).isEqualTo("TX");

        service.advance(s.id, null, null);
        assertThat(s.status).isEqualTo(ShipmentStatus.WON);
        service.advance(s.id, carrier.id, new BigDecimal("1600.00"));
        service.advance(s.id, null, null);
        service.advance(s.id, null, null);
        assertThat(s.status).isEqualTo(ShipmentStatus.DELIVERED);

        companies.flush();
        BigDecimal margin = jdbc.queryForObject("select margin from shipment where id = ?", BigDecimal.class, s.id);
        assertThat(margin).isEqualByComparingTo("400.00");
        assertThatThrownBy(() -> service.advance(s.id, null, null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void coveringNeedsACarrierAndCost() {
        Shipment s = quote(company("Ship2", CompanyType.SHIPPER));
        service.advance(s.id, null, null);
        assertThatThrownBy(() -> service.advance(s.id, null, null)).isInstanceOf(IllegalArgumentException.class);
        Company notCarrier = company("Broker", CompanyType.BROKER);
        assertThatThrownBy(() -> service.advance(s.id, notCarrier.id, BigDecimal.TEN)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lostNeedsReason() {
        Shipment s = quote(company("Ship3", CompanyType.SHIPPER));
        assertThatThrownBy(() -> service.lose(s.id, " ")).isInstanceOf(IllegalArgumentException.class);
        service.lose(s.id, "Price");
        assertThat(s.status).isEqualTo(ShipmentStatus.LOST);
    }

    @Test
    void rejectsOverweightAndBackwardsDates() {
        Company shipper = company("Ship4", CompanyType.SHIPPER);
        Instant t = Instant.now();
        assertThatThrownBy(() -> service.quote(shipper.id, "A", "TX", "B", "CO", Equipment.DRY_VAN, 48001, t, t, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.quote(shipper.id, "A", "TX", "B", "CO", Equipment.DRY_VAN, 1000, t, t.minusSeconds(1), BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void databaseEnforcesWeightLimit() {
        Company shipper = company("Ship5", CompanyType.SHIPPER);
        assertThatThrownBy(() -> jdbc.update("""
                insert into shipment (shipper_id, origin_city, origin_state, dest_city, dest_state, equipment, weight_lbs,
                                      pickup_at, delivery_at, customer_rate)
                values (?, 'A', 'TX', 'B', 'CO', 'DRY_VAN', 50000, now(), now(), 1)""", shipper.id))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
