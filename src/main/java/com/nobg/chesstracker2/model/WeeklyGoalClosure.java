package com.nobg.chesstracker2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "weekly_goal_closures",
        uniqueConstraints = @UniqueConstraint(name = "uq_weekly_goal_closures_iso_week", columnNames = {"iso_year", "iso_week"})
)
public class WeeklyGoalClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int isoYear;

    @Column(nullable = false)
    private int isoWeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeeklyGoalClosureStatus status = WeeklyGoalClosureStatus.NOT_RECORDED;

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

    public int getIsoYear() {
        return isoYear;
    }

    public void setIsoYear(int isoYear) {
        this.isoYear = isoYear;
    }

    public int getIsoWeek() {
        return isoWeek;
    }

    public void setIsoWeek(int isoWeek) {
        this.isoWeek = isoWeek;
    }

    public WeeklyGoalClosureStatus getStatus() {
        return status;
    }

    public void setStatus(WeeklyGoalClosureStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
