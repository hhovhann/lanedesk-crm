package com.lanedesk.config;

import java.math.BigDecimal;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("lanedesk")
public record LaneDeskProperties(Security security, BigDecimal commissionPct, ZoneId agentTimeZone, Targets targets) {

    public record Security(String user, String password) {}

    public record Targets(int callsPerDay, int conversationsPerDay, int quotesPerWeek, int loadsPerWeek) {}
}
