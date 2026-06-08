package com.nobg.chesstracker2.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.nobg.chesstracker2.dto.TrainingDayForm;
import com.nobg.chesstracker2.dto.TrainingEntryForm;
import com.nobg.chesstracker2.model.DailyCompletionStatus;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyNoteRepository;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.viewmodel.DaySummaryViewModel;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TrainingEntryServiceIntegrationTest {

    @Autowired
    private TrainingEntryService service;

    @Autowired
    private TrainingCategoryRepository categoryRepository;

    @Autowired
    private DailyTrainingEntryRepository entryRepository;

    @Autowired
    private DailyNoteRepository noteRepository;

    @BeforeEach
    void cleanEntries() {
        entryRepository.deleteAll();
        noteRepository.deleteAll();
    }

    @Test
    void saveDayStoresEntryAndSummaryCountsIt() {
        LocalDate date = LocalDate.of(2026, 6, 8);
        TrainingCategory tactics = tactics();

        TrainingEntryForm entry = new TrainingEntryForm();
        entry.setCategoryId(tactics.getId());
        entry.setTrained(true);
        entry.setResult("3/5");
        entry.setDurationMinutes(5);
        entry.setNote("Test");

        TrainingDayForm form = new TrainingDayForm();
        form.setEntries(List.of(entry));

        boolean saved = service.saveDay(date, form);

        DaySummaryViewModel summary = service.daySummary(date);
        assertThat(saved).isTrue();
        assertThat(summary.trainedCategoryCount()).isEqualTo(1);
        assertThat(summary.successCount()).isEqualTo(3);
        assertThat(summary.totalCount()).isEqualTo(5);
        assertThat(summary.successRate()).isEqualTo(60);
        assertThat(summary.totalDurationMinutes()).isEqualTo(5);
        assertThat(summary.completionStatus()).isEqualTo(DailyCompletionStatus.PARTIAL);
        assertThat(summary.copyBlock()).contains("Tactics: 3/5 = 60%, Zeit: 5 min, Notiz: Test");
        assertThat(summary.copyBlock()).contains("Tagesstatus: Teilweise bearbeitet / nicht abgeschlossen");
    }

    @Test
    void completeDayStoresCompletionLockAndCopyBlockDisplaysIt() {
        LocalDate date = LocalDate.of(2026, 6, 9);
        TrainingCategory tactics = tactics();

        TrainingEntryForm entry = new TrainingEntryForm();
        entry.setCategoryId(tactics.getId());
        entry.setTrained(true);
        entry.setResult("4/5");
        entry.setDurationMinutes(10);

        TrainingDayForm form = new TrainingDayForm();
        form.setEntries(List.of(entry));

        boolean completed = service.completeDay(date, form);

        DaySummaryViewModel summary = service.daySummary(date);
        assertThat(completed).isTrue();
        assertThat(summary.completionStatus()).isEqualTo(DailyCompletionStatus.COMPLETED);
        assertThat(summary.locked()).isTrue();
        assertThat(summary.completedAt()).isNotNull();
        assertThat(summary.copyBlock()).contains("Tagesstatus: Abgeschlossen");
        assertThat(noteRepository.findByTrainingDate(date).orElseThrow().getCompletedAt()).isNotNull();
    }

    @Test
    void saveDayDoesNotChangeLockedDay() {
        LocalDate date = LocalDate.of(2026, 6, 10);
        TrainingCategory tactics = tactics();

        TrainingEntryForm entry = new TrainingEntryForm();
        entry.setCategoryId(tactics.getId());
        entry.setTrained(true);
        entry.setResult("3/5");
        entry.setDurationMinutes(5);

        TrainingDayForm form = new TrainingDayForm();
        form.setEntries(List.of(entry));
        service.completeDay(date, form);

        TrainingEntryForm changedEntry = new TrainingEntryForm();
        changedEntry.setCategoryId(tactics.getId());
        changedEntry.setTrained(true);
        changedEntry.setResult("5/5");
        changedEntry.setDurationMinutes(20);

        TrainingDayForm changedForm = new TrainingDayForm();
        changedForm.setEntries(List.of(changedEntry));
        boolean saved = service.saveDay(date, changedForm);

        assertThat(saved).isFalse();
        var savedEntry = entryRepository.findByTrainingDateAndCategoryId(date, tactics.getId()).orElseThrow();
        assertThat(savedEntry.getSuccessCount()).isEqualTo(3);
        assertThat(savedEntry.getTotalCount()).isEqualTo(5);
        assertThat(savedEntry.getDurationMinutes()).isEqualTo(5);
        assertThat(entryRepository.findByTrainingDateOrderByCategorySortOrderAsc(date)).hasSize(1);
    }

    private TrainingCategory tactics() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .filter(category -> "Tactics".equals(category.getName()))
                .findFirst()
                .orElseThrow();
    }
}
