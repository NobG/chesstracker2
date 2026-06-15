package com.nobg.chesstracker2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nobg.chesstracker2.model.DailyCompletionStatus;
import com.nobg.chesstracker2.model.DailyNote;
import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyNoteRepository;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.viewmodel.MonthlyStatsViewModel;
import com.nobg.chesstracker2.viewmodel.WeeklyStatsViewModel;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatsServiceTest {

    private final DailyTrainingEntryRepository entryRepository = org.mockito.Mockito.mock(DailyTrainingEntryRepository.class);
    private final TrainingCategoryRepository categoryRepository = org.mockito.Mockito.mock(TrainingCategoryRepository.class);
    private final DailyNoteRepository noteRepository = org.mockito.Mockito.mock(DailyNoteRepository.class);
    private final AppDateProvider appDateProvider = org.mockito.Mockito.mock(AppDateProvider.class);
    private final StatsService service = new StatsService(entryRepository, categoryRepository, noteRepository, appDateProvider);

    @Test
    void buildsWeeklyStats() {
        TrainingCategory tactics = category("Tactics", 1);
        LocalDate start = LocalDate.of(2026, 6, 8);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(tactics));
        when(entryRepository.findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(start, start.plusDays(6)))
                .thenReturn(List.of(entry(tactics, start, 7, 10, 15), entry(tactics, start.plusDays(1), 8, 10, 20)));
        when(noteRepository.findByTrainingDateBetween(start, start.plusDays(6)))
                .thenReturn(List.of(note(start, DailyCompletionStatus.COMPLETED), note(start.plusDays(1), DailyCompletionStatus.PARTIAL)));

        WeeklyStatsViewModel stats = service.weekStats(2026, 24);

        assertThat(stats.trainingDays()).isEqualTo(2);
        assertThat(stats.totalTasks()).isEqualTo(20);
        assertThat(stats.successRate()).isEqualTo(75);
        assertThat(stats.bestCategory()).isEqualTo("Tactics");
        assertThat(stats.completedTrainingDays()).isEqualTo(1);
        assertThat(stats.partialTrainingDays()).isEqualTo(1);
        assertThat(stats.openTrainingDaysWithEntries()).isZero();
    }

    @Test
    void weeklyStatsIgnoreTacticsChallengeForTasksAndRates() {
        TrainingCategory tactics = category("tactics", "Tactics", 1);
        TrainingCategory challenge = category("tactics-challenge", "Tactics Challenge", 2);
        LocalDate start = LocalDate.of(2026, 6, 8);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(tactics, challenge));
        when(entryRepository.findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(start, start.plusDays(6)))
                .thenReturn(List.of(
                        entry(tactics, start, 3, 8, 12),
                        entry(challenge, start, 15, 18, 3)
                ));
        when(noteRepository.findByTrainingDateBetween(start, start.plusDays(6))).thenReturn(List.of());

        WeeklyStatsViewModel stats = service.weekStats(2026, 24);

        assertThat(stats.trainingDays()).isEqualTo(1);
        assertThat(stats.trainedCategories()).isEqualTo(2);
        assertThat(stats.totalTasks()).isEqualTo(8);
        assertThat(stats.totalDurationMinutes()).isEqualTo(15);
        assertThat(stats.successRate()).isEqualTo(38);
        assertThat(stats.days().getFirst().trainedCategories()).isEqualTo(2);
        assertThat(stats.days().getFirst().totalTasks()).isEqualTo(8);
        assertThat(stats.days().getFirst().successRate()).isEqualTo(38);
        assertThat(stats.categories()).filteredOn("categoryName", "Tactics Challenge")
                .first()
                .extracting("successRate")
                .isNull();
    }

    @Test
    void buildsMonthlyStats() {
        TrainingCategory tactics = category("Tactics", 1);
        LocalDate first = LocalDate.of(2026, 6, 1);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(tactics));
        when(entryRepository.findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(first, LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(entry(tactics, first, 5, 10, 10), entry(tactics, first.plusDays(7), 8, 10, 12)));
        when(noteRepository.findByTrainingDateBetween(first, LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(note(first, DailyCompletionStatus.COMPLETED), note(first.plusDays(7), DailyCompletionStatus.PARTIAL)));

        MonthlyStatsViewModel stats = service.monthStats(2026, 6);

        assertThat(stats.trainingDays()).isEqualTo(2);
        assertThat(stats.totalTasks()).isEqualTo(20);
        assertThat(stats.successRate()).isEqualTo(65);
        assertThat(stats.completedTrainingDays()).isEqualTo(1);
        assertThat(stats.partialTrainingDays()).isEqualTo(1);
        assertThat(stats.completionRate()).isEqualTo(50);
        assertThat(stats.improvedCategories()).contains("Tactics");
    }

    @Test
    void monthlyStatsIgnoreTacticsChallengeForTasksAndRates() {
        TrainingCategory tactics = category("tactics", "Tactics", 1);
        TrainingCategory challenge = category("tactics-challenge", "Tactics Challenge", 2);
        LocalDate first = LocalDate.of(2026, 6, 1);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(tactics, challenge));
        when(entryRepository.findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(first, LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(
                        entry(tactics, first, 3, 8, 12),
                        entry(challenge, first, 15, 18, 3)
                ));
        when(noteRepository.findByTrainingDateBetween(first, LocalDate.of(2026, 6, 30))).thenReturn(List.of());

        MonthlyStatsViewModel stats = service.monthStats(2026, 6);

        assertThat(stats.trainingDays()).isEqualTo(1);
        assertThat(stats.totalTasks()).isEqualTo(8);
        assertThat(stats.totalDurationMinutes()).isEqualTo(15);
        assertThat(stats.successRate()).isEqualTo(38);
        assertThat(stats.categories()).filteredOn("categoryName", "Tactics Challenge")
                .first()
                .extracting("successRate")
                .isNull();
    }

    @Test
    void categoryOverviewEvaluatesTacticsChallengeBySolvedTasks() {
        TrainingCategory challenge = category("tactics-challenge", "Tactics Challenge", 1);
        LocalDate first = LocalDate.of(2026, 6, 1);
        LocalDate second = LocalDate.of(2026, 6, 2);
        LocalDate third = LocalDate.of(2026, 6, 3);
        when(appDateProvider.today()).thenReturn(third);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(challenge));
        when(entryRepository.findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(third.minusYears(5), third))
                .thenReturn(List.of(
                        entry(challenge, first, 36, 39, 10),
                        entry(challenge, second, 24, 27, 8),
                        entry(challenge, third, 42, 45, 10)
                ));

        var stats = service.categoryOverview();

        assertThat(stats).hasSize(1);
        var challengeStats = stats.getFirst();
        assertThat(challengeStats.challenge()).isTrue();
        assertThat(challengeStats.challengeBestSolved()).isEqualTo(42);
        assertThat(challengeStats.bestDay()).isEqualTo(third);
        assertThat(challengeStats.bestDayRate()).isEqualTo(42);
        assertThat(challengeStats.worstDay()).isEqualTo(second);
        assertThat(challengeStats.worstDayRate()).isEqualTo(24);
        assertThat(challengeStats.trend()).isEqualTo("steigend");
    }

    private TrainingCategory category(String name, int sortOrder) {
        return category(name.toLowerCase(), name, sortOrder);
    }

    private TrainingCategory category(String key, String name, int sortOrder) {
        TrainingCategory category = new TrainingCategory();
        category.setKey(key);
        category.setName(name);
        category.setSortOrder(sortOrder);
        category.setActive(true);
        return category;
    }

    private DailyTrainingEntry entry(TrainingCategory category, LocalDate date, int success, int total, int minutes) {
        DailyTrainingEntry entry = new DailyTrainingEntry();
        entry.setTrainingDate(date);
        entry.setCategory(category);
        entry.setTrained(true);
        entry.setSuccessCount(success);
        entry.setTotalCount(total);
        entry.setDurationMinutes(minutes);
        return entry;
    }

    private DailyNote note(LocalDate date, DailyCompletionStatus status) {
        DailyNote note = new DailyNote();
        note.setTrainingDate(date);
        note.setCompletionStatus(status);
        if (status == DailyCompletionStatus.COMPLETED) {
            note.setCompletedAt(OffsetDateTime.now());
        }
        return note;
    }
}
