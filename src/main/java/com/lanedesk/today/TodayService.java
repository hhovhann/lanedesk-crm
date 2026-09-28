package com.lanedesk.today;

import com.lanedesk.activity.Activity;
import com.lanedesk.activity.ActivityRepository;
import com.lanedesk.company.CompanyStatus;
import com.lanedesk.config.LaneDeskProperties;
import com.lanedesk.contact.Contact;
import com.lanedesk.contact.ContactRepository;
import com.lanedesk.shipment.Shipment;
import com.lanedesk.shipment.ShipmentRepository;
import com.lanedesk.shipment.ShipmentStatus;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodayService {

    static final int OPEN_HOUR = 8;
    static final int CLOSE_HOUR = 17;
    static final int CALL_LIST_LIMIT = 40;

    private final ActivityRepository activities;
    private final ContactRepository contacts;
    private final ShipmentRepository shipments;
    private final LaneDeskProperties props;

    public TodayService(ActivityRepository activities, ContactRepository contacts, ShipmentRepository shipments,
                        LaneDeskProperties props) {
        this.activities = activities;
        this.contacts = contacts;
        this.shipments = shipments;
        this.props = props;
    }

    public record FollowUp(Activity activity, Contact contact, boolean callableNow) {}

    public record CallTarget(Contact contact, boolean hasOpenFollowUp) {}

    public record View(List<FollowUp> followUps, List<CallTarget> callList, List<Shipment> loads,
                       int callsToday, int callsTarget, int conversationsToday, int conversationsTarget) {}

    public View build() {
        Instant now = Instant.now();
        ZonedDateTime agentNow = now.atZone(props.agentTimeZone());
        Instant startOfDay = agentNow.toLocalDate().atStartOfDay(props.agentTimeZone()).toInstant();

        List<Activity> due = activities.dueFollowUps(now);
        List<FollowUp> followUps = new ArrayList<>();
        Set<Long> dueCompanies = new HashSet<>();
        for (Activity a : due) {
            if (!dueCompanies.add(a.company.id)) {
                continue;
            }
            Contact c = a.contact != null ? a.contact : contacts.findByCompanyIdOrderByName(a.company.id).stream().findFirst().orElse(null);
            followUps.add(new FollowUp(a, c, c != null && isCallable(c, now)));
        }
        followUps.sort(Comparator.comparing((FollowUp f) -> !f.callableNow()).thenComparing(f -> f.activity().nextFollowUpAt));

        Set<Long> calledToday = new HashSet<>(activities.companyIdsCalledSince(startOfDay));
        List<CallTarget> callList = contacts.findByPhoneIsNotNullAndCompanyStatusNotIn(
                        List.of(CompanyStatus.DO_NOT_CALL, CompanyStatus.INACTIVE)).stream()
                .filter(c -> isCallable(c, now))
                .filter(c -> !calledToday.contains(c.company.id))
                .filter(c -> !dueCompanies.contains(c.company.id))
                .sorted(Comparator.comparing((Contact c) -> c.company.status.ordinal()).reversed()
                        .thenComparing(c -> c.company.name))
                .limit(CALL_LIST_LIMIT)
                .map(c -> new CallTarget(c, false))
                .toList();

        List<Shipment> loads = new ArrayList<>(shipments.findByStatusOrderByPickupAt(ShipmentStatus.IN_TRANSIT));
        loads.addAll(shipments.findByStatusOrderByPickupAt(ShipmentStatus.COVERED));

        List<Activity> calls = activities.callsBetween(startOfDay, now.plusSeconds(1));
        int conversations = (int) calls.stream().filter(a -> a.outcome == com.lanedesk.activity.Outcome.CONVERSATION).count();
        return new View(followUps, callList, loads, calls.size(), props.targets().callsPerDay(),
                conversations, props.targets().conversationsPerDay());
    }

    /** True when it is Mon–Fri, 08:00–17:00 in the contact's own time zone. */
    static boolean isCallable(Contact c, Instant now) {
        ZonedDateTime local = now.atZone(ZoneId.of(c.timeZone));
        DayOfWeek d = local.getDayOfWeek();
        return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY
                && local.getHour() >= OPEN_HOUR && local.getHour() < CLOSE_HOUR;
    }
}
