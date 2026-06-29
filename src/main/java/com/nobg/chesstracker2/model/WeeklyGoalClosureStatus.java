package com.nobg.chesstracker2.model;

public enum WeeklyGoalClosureStatus {
    NOT_RECORDED("Aimchess Wochenabschluss noch nicht erfasst", "neutral"),
    ACHIEVED("Aimchess Wochenziel erreicht", "positive"),
    MISSED("Aimchess Wochenziel nicht erreicht", "negative");

    private final String displayText;
    private final String tone;

    WeeklyGoalClosureStatus(String displayText, String tone) {
        this.displayText = displayText;
        this.tone = tone;
    }

    public String displayText() {
        return displayText;
    }

    public String tone() {
        return tone;
    }
}
