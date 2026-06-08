package com.nobg.chesstracker2.dto;

import java.time.LocalDate;

public class RatingSnapshotForm {

    private LocalDate snapshotDate = LocalDate.now();

    private Integer lichessBlitz;

    private Integer lichessRapid;

    private Integer lichessClassical;

    private Integer dwz;

    private Integer fideElo;

    private String note;

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public Integer getLichessBlitz() {
        return lichessBlitz;
    }

    public void setLichessBlitz(Integer lichessBlitz) {
        this.lichessBlitz = lichessBlitz;
    }

    public Integer getLichessRapid() {
        return lichessRapid;
    }

    public void setLichessRapid(Integer lichessRapid) {
        this.lichessRapid = lichessRapid;
    }

    public Integer getLichessClassical() {
        return lichessClassical;
    }

    public void setLichessClassical(Integer lichessClassical) {
        this.lichessClassical = lichessClassical;
    }

    public Integer getDwz() {
        return dwz;
    }

    public void setDwz(Integer dwz) {
        this.dwz = dwz;
    }

    public Integer getFideElo() {
        return fideElo;
    }

    public void setFideElo(Integer fideElo) {
        this.fideElo = fideElo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
