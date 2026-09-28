package com.lanedesk.shipment;

public enum ShipmentStatus {
    QUOTED, WON, COVERED, IN_TRANSIT, DELIVERED, LOST;

    public ShipmentStatus next() {
        return switch (this) {
            case QUOTED -> WON;
            case WON -> COVERED;
            case COVERED -> IN_TRANSIT;
            case IN_TRANSIT -> DELIVERED;
            case DELIVERED, LOST -> null;
        };
    }

    public boolean isBooked() {
        return this == WON || this == COVERED || this == IN_TRANSIT || this == DELIVERED;
    }
}
