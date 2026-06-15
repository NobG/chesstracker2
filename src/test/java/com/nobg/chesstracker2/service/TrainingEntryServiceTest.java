package com.nobg.chesstracker2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyNoteRepository;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.viewmodel.DaySummaryViewModel;
import com.nobg.chesstracker2.viewmodel.TodayViewModel;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TrainingEntryServiceTest {

    private final TrainingCategoryRepository categoryRepository = org.mockito.Mockito.mock(TrainingCategoryRepository.class);
    private final DailyTrainingEntryRepository entryRepository = org.mockito.Mockito.mock(DailyTrainingEntryRepository.class);
    private final DailyNoteRepository noteRepository = org.mockito.Mockito.mock(DailyNoteRepository.class);
    private final TrainingEntryService service = new TrainingEntryService(categoryRepository, entryRepository, noteRepository);

    @Test
    void buildsDaySummaryWithTotalsAndCopyBlock() {
        LocalDate date = LocalDate.of(2026, 6, 8);
        when(entryRepository.findByTrainingDateOrderByCategorySortOrderAsc(date))
                .thenReturn(List.of(entry("Tactics", 7, 10, 15), entry("Calculation", 3, 5, 10)));
        when(noteRepository.findByTrainingDate(date)).thenReturn(Optional.empty());

        DaySummaryViewModel summary = service.daySummary(date);

        assertThat(summary.successCount()).isEqualTo(10);
        assertThat(summary.totalCount()).isEqualTo(15);
        assertThat(summary.successRate()).isEqualTo(67);
        assertThat(summary.totalDurationMinutes()).isEqualTo(25);
        assertThat(summary.copyBlock()).contains("Aimchess Training - 2026-06-08", "Tactics: 7/10 = 70%");
    }

    @Test
    void daySummaryTreatsTacticsChallengeAsPointsOnly() {
        LocalDate date = LocalDate.of(2026, 6, 8);
        TrainingCategory tactics = category(1L, "tactics", "Tactics", 20);
        TrainingCategory challenge = category(2L, "tactics-challenge", "Tactics Challenge", 150);
        when(entryRepository.findByTrainingDateOrderByCategorySortOrderAsc(date))
                .thenReturn(List.of(
                        entry(tactics, true, 3, 8, null, 12, null),
                        entry(challenge, true, 15, 18, null, 3, null)
                ));
        when(noteRepository.findByTrainingDate(date)).thenReturn(Optional.empty());

        DaySummaryViewModel summary = service.daySummary(date);

        assertThat(summary.trainedCategoryCount()).isEqualTo(2);
        assertThat(summary.successCount()).isEqualTo(3);
        assertThat(summary.totalCount()).isEqualTo(8);
        assertThat(summary.successRate()).isEqualTo(38);
        assertThat(summary.totalDurationMinutes()).isEqualTo(15);
        assertThat(summary.copyBlock()).contains(
                "Tactics: 3/8 = 38%, Zeit: 12 min",
                "Tactics Challenge: Punkte: 15, Zeit: 3 min",
                "- Aufgaben: 3/8 = 38%"
        );
        assertThat(summary.copyBlock()).doesNotContain("18/26", "15/18 = 83%");
    }

    @Test
    void todayViewMarksWorkedCategoriesAndKeepsEmptyEntriesUnmarked() {
        LocalDate date = LocalDate.of(2026, 6, 8);
        TrainingCategory tactics = category(1L, "tactics", "Tactics", 20);
        TrainingCategory opening = category(2L, "opening-improver", "Opening Improver", 30);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(tactics, opening));
        when(entryRepository.findByTrainingDateOrderByCategorySortOrderAsc(date))
                .thenReturn(List.of(
                        entry(tactics, true, 0, 0, null, null, null),
                        entry(opening, false, 0, 0, null, null, " ")
                ));
        when(noteRepository.findByTrainingDate(date)).thenReturn(Optional.empty());

        TodayViewModel view = service.todayView(date);

        assertThat(view.entries()).extracting("categoryName", "workedToday")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Tactics", true),
                        org.assertj.core.groups.Tuple.tuple("Opening Improver", false)
                );
    }

    @Test
    void todayViewSortsWorkedCategoriesFirstAndPreservesAimchessOrderWithinGroups() {
        LocalDate date = LocalDate.of(2026, 6, 8);
        TrainingCategory advantage = category(1L, "advantage-capitalization", "Advantage Capitalization", 10);
        TrainingCategory tactics = category(2L, "tactics", "Tactics", 20);
        TrainingCategory opening = category(3L, "opening-improver", "Opening Improver", 30);
        TrainingCategory time = category(4L, "time-trainer", "Time Trainer", 110);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(advantage, tactics, opening, time));
        when(entryRepository.findByTrainingDateOrderByCategorySortOrderAsc(date))
                .thenReturn(List.of(
                        entry(tactics, false, 3, 5, null, null, null),
                        entry(time, false, 0, 0, null, 5, null)
                ));
        when(noteRepository.findByTrainingDate(date)).thenReturn(Optional.empty());

        TodayViewModel view = service.todayView(date);

        assertThat(view.entries()).extracting("categoryName")
                .containsExactly("Tactics", "Time Trainer", "Advantage Capitalization", "Opening Improver");
        assertThat(view.form().getEntries()).extracting("categoryId")
                .containsExactly(tactics.getId(), time.getId(), advantage.getId(), opening.getId());
    }

    private DailyTrainingEntry entry(String categoryName, int success, int total, int minutes) {
        TrainingCategory category = new TrainingCategory();
        category.setName(categoryName);
        category.setKey(categoryName.toLowerCase());
        category.setSortOrder(1);
        DailyTrainingEntry entry = new DailyTrainingEntry();
        entry.setTrainingDate(LocalDate.of(2026, 6, 8));
        entry.setCategory(category);
        entry.setTrained(true);
        entry.setSuccessCount(success);
        entry.setTotalCount(total);
        entry.setDurationMinutes(minutes);
        return entry;
    }

    private TrainingCategory category(Long id, String key, String name, int sortOrder) {
        TrainingCategory category = new TrainingCategory();
        ReflectionTestUtils.setField(category, "id", id);
        category.setKey(key);
        category.setName(name);
        category.setSortOrder(sortOrder);
        category.setActive(true);
        return category;
    }

    private DailyTrainingEntry entry(
            TrainingCategory category,
            boolean trained,
            int success,
            int total,
            Integer score,
            Integer minutes,
            String note
    ) {
        DailyTrainingEntry entry = new DailyTrainingEntry();
        entry.setTrainingDate(LocalDate.of(2026, 6, 8));
        entry.setCategory(category);
        entry.setTrained(trained);
        entry.setSuccessCount(success);
        entry.setTotalCount(total);
        entry.setScore(score);
        entry.setDurationMinutes(minutes);
        entry.setNote(note);
        return entry;
    }
}
