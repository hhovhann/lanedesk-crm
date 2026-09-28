package com.lanedesk.activity;

public enum Outcome {
    NO_ANSWER("No answer"), VOICEMAIL("Voicemail"), CONVERSATION("Talked"), NOT_INTERESTED("Not interested");

    private final String label;

    Outcome(String label) { this.label = label; }

    public String getLabel() { return label; }
}
