package com.lanedesk.stats;

import com.lanedesk.activity.Activity;
import com.lanedesk.activity.ActivityRepository;
import com.lanedesk.activity.Outcome;
import com.lanedesk.config.LaneDeskProperties;
import com.lanedesk.shipment.Shipment;
import com.lanedesk.shipment.ShipmentRepository;
import com.lanedesk.shipment.ShipmentStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private static final Set<ShipmentStatus> BOOKED = EnumSet.of(ShipmentStatus.WON, ShipmentStatus.COVERED,
            ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED);

    private final ActivityRepository activities;
    private final ShipmentRepository shipments;
    private final LaneDeskProperties props;

    public StatsService(ActivityRepository activities, ShipmentRepository shipments, LaneDeskProperties props) {
        this.activities = activities;
        this.shipments = shipments;
        this.props = props;
    }

    public record Day(LocalDate date, int calls, int conversations) {}

    public record Metric(String label, BigDecimal value, BigDecimal target, String unit) {
        public int percent() {
            if (target == null || target.signum() == 0) {
                return 0;
            }
            return Math.min(100, value.multiply(BigDecimal.valueOf(100)).divide(target, 0, RoundingMode.HALF_UP).intValue());
        }
    }

    public record View(Metric callsToday, Metric conversationsToday, Metric quotesWeek, Metric loadsWeek,
                       BigDecimal winRatePct, int won, int lost, BigDecimal marginWeek, BigDecimal commissionWeek,
                       BigDecimal marginMonth, BigDecimal commissionMonth, List<Day> days, BigDecimal commissionPct) {}

    public View build() {
        ZoneId zone = props.agentTimeZone();
        Instant now = Instant.now();
        LocalDate today = now.atZone(zone).toLocalDate();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Instant weekFrom = weekStart.atStartOfDay(zone).toInstant();
        Instant weekTo = weekStart.plusWeeks(1).atStartOfDay(zone).toInstant();
        LocalDate monthStart = today.withDayOfMonth(1);
        Instant monthFrom = monthStart.atStartOfDay(zone).toInstant();
        Instant monthTo = monthStart.plusMonths(1).atStartOfDay(zone).toInstant();

        List<Day> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(i);
            List<Activity> calls = activities.callsBetween(d.atStartOfDay(zone).toInstant(), d.plusDays(1).atStartOfDay(zone).toInstant());
            days.add(new Day(d, calls.size(), (int) calls.stream().filter(a -> a.outcome == Outcome.CONVERSATION).count()));
        }

        List<Shipment> bookedWeek = shipments.findByStatusInAndPickupAtBetween(BOOKED, weekFrom, weekTo);
        List<Shipment> bookedMonth = shipments.findByStatusInAndPickupAtBetween(BOOKED, monthFrom, monthTo);
        BigDecimal marginWeek = margin(bookedWeek);
        BigDecimal marginMonth = margin(bookedMonth);

        int won = 0;
        int lost = 0;
        for (Shipment s : shipments.findAll()) {
            if (s.status == ShipmentStatus.LOST) {
                lost++;
            } else if (BOOKED.contains(s.status)) {
                won++;
            }
        }
        BigDecimal winRate = won + lost == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(won * 100.0 / (won + lost)).setScale(0, RoundingMode.HALF_UP);

        var t = props.targets();
        return new View(
                new Metric("Calls today", BigDecimal.valueOf(days.get(0).calls()), BigDecimal.valueOf(t.callsPerDay()), ""),
                new Metric("Conversations today", BigDecimal.valueOf(days.get(0).conversations()), BigDecimal.valueOf(t.conversationsPerDay()), ""),
                new Metric("Quotes this week", BigDecimal.valueOf(shipments.countByQuotedAtBetween(weekFrom, weekTo)), BigDecimal.valueOf(t.quotesPerWeek()), ""),
                new Metric("Loads this week", BigDecimal.valueOf(bookedWeek.size()), BigDecimal.valueOf(t.loadsPerWeek()), ""),
                winRate, won, lost, marginWeek, commission(marginWeek), marginMonth, commission(marginMonth), days,
                props.commissionPct().multiply(BigDecimal.valueOf(100)).stripTrailingZeros());
    }

    private BigDecimal margin(List<Shipment> list) {
        return list.stream().map(s -> s.margin).filter(m -> m != null).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal commission(BigDecimal margin) {
        return margin.multiply(props.commissionPct()).setScale(2, RoundingMode.HALF_UP);
    }
}
