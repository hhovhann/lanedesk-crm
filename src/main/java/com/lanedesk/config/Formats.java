package com.lanedesk.config;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

/** Template helpers, exposed as {@code @fmt}. Everything is stored UTC and rendered in a zone chosen here. */
@Component("fmt")
public class Formats {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("EEE MMM d, h:mm a");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public static final List<String> US_ZONES = List.of("America/New_York", "America/Chicago", "America/Denver",
            "America/Phoenix", "America/Los_Angeles", "America/Anchorage", "Pacific/Honolulu");

    private final ZoneId agentZone;

    public Formats(LaneDeskProperties props) {
        this.agentZone = props.agentTimeZone();
    }

    public String agent(Instant t) {
        return t == null ? "" : DATE_TIME.format(t.atZone(agentZone));
    }

    public String in(Instant t, String zone) {
        return t == null ? "" : DATE_TIME.format(t.atZone(ZoneId.of(zone)));
    }

    public String clock(String zone) {
        return TIME.format(Instant.now().atZone(ZoneId.of(zone)));
    }

    public String zoneShort(String zone) {
        return switch (zone) {
            case "America/New_York" -> "ET";
            case "America/Chicago" -> "CT";
            case "America/Denver", "America/Phoenix" -> "MT";
            case "America/Los_Angeles" -> "PT";
            case "America/Anchorage" -> "AKT";
            case "Pacific/Honolulu" -> "HT";
            default -> zone;
        };
    }

    public String agentZoneName() {
        return agentZone.getId();
    }

    public String input(Instant t) {
        return INPUT.format(t.atZone(agentZone));
    }

    public List<String> zones() {
        return US_ZONES;
    }
}
