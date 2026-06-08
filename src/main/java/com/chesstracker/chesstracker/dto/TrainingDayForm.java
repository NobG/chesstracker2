package com.chesstracker.chesstracker.dto;

import java.util.ArrayList;
import java.util.List;

public class TrainingDayForm {

    private String dayNote;
    private List<TrainingEntryForm> entries = new ArrayList<>();

    public String getDayNote() {
        return dayNote;
    }

    public void setDayNote(String dayNote) {
        this.dayNote = dayNote;
    }

    public List<TrainingEntryForm> getEntries() {
        return entries;
    }

    public void setEntries(List<TrainingEntryForm> entries) {
        this.entries = entries;
    }
}
