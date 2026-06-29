package com.nobg.chesstracker2.dto;

public class WeeklyGoalClosureForm {

    private String status = "NOT_RECORDED";

    private String note;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
