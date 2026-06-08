package com.nobg.chesstracker2.model;

public enum DailyCompletionStatus {
    OPEN("Offen"),
    PARTIAL("Teilweise bearbeitet"),
    COMPLETED("Abgeschlossen");

    private final String displayLabel;

    DailyCompletionStatus(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String displayLabel() {
        return displayLabel;
    }
}
