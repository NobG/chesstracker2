package com.nobg.chesstracker2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "rating_snapshots",
        uniqueConstraints = @UniqueConstraint(name = "uq_rating_snapshots_snapshot_date", columnNames = "snapshot_date")
)
public class RatingSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    private Integer lichessBlitz;

    private Integer lichessRapid;

    private Integer lichessClassical;

    private Integer dwz;

    @Column(name = "fide_elo")
    private Integer fideElo;

    private Integer tacticsRating;

    private Integer endgameRating;

    private String note;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

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

    public Integer getTacticsRating() {
        return tacticsRating;
    }

    public void setTacticsRating(Integer tacticsRating) {
        this.tacticsRating = tacticsRating;
    }

    public Integer getEndgameRating() {
        return endgameRating;
    }

    public void setEndgameRating(Integer endgameRating) {
        this.endgameRating = endgameRating;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
