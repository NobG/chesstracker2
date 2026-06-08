package com.nobg.chesstracker2.dto;

import com.nobg.chesstracker2.model.DailyCompletionStatus;
import java.util.ArrayList;
import java.util.List;

public class TrainingDayForm {

    private String dayNote;
    private DailyCompletionStatus completionStatus = DailyCompletionStatus.OPEN;
    private List<TrainingEntryForm> entries = new ArrayList<>();

    public String getDayNote() {
        return dayNote;
    }

    public void setDayNote(String dayNote) {
        this.dayNote = dayNote;
    }

    public DailyCompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(DailyCompletionStatus completionStatus) {
        this.completionStatus = completionStatus == null ? DailyCompletionStatus.OPEN : completionStatus;
    }

    public List<TrainingEntryForm> getEntries() {
        return entries;
    }

    public void setEntries(List<TrainingEntryForm> entries) {
        this.entries = entries;
    }
}
