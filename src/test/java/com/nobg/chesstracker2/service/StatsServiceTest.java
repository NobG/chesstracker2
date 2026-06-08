package com.nobg.chesstracker2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.viewmodel.MonthlyStatsViewModel;
import com.nobg.chesstracker2.viewmodel.WeeklyStatsViewModel;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatsServiceTest {

    private final DailyTrainingEntryRepository entryRepository = org.mockito.Mockito.mock(DailyTrainingEntryRepository.class);
    private final TrainingCategoryRepository categoryRepository = org.mockito.Mockito.mock(TrainingCategoryRepository.class);
    private final StatsService service = new StatsService(entryRepository, categoryRepository);

    @Test
    void buildsWeeklyStats() {
        TrainingCategory tactics = category("Tactics", 1);
        LocalDate start = LocalDate.of(2026, 6, 8);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(tactics));
        when(entryRepository.findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(start, start.plusDays(6)))
                .thenReturn(List.of(entry(tactics, start, 7, 10, 15), entry(tactics, start.plusDays(1), 8, 10, 20)));

        WeeklyStatsViewModel stats = service.weekStats(2026, 24);

        assertThat(stats.trainingDays()).isEqualTo(2);
        assertThat(stats.totalTasks()).isEqualTo(20);
        assertThat(stats.successRate()).isEqualTo(75);
        assertThat(stats.bestCategory()).isEqualTo("Tactics");
    }

    @Test
    void buildsMonthlyStats() {
        TrainingCategory tactics = category("Tactics", 1);
        LocalDate first = LocalDate.of(2026, 6, 1);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(tactics));
        when(entryRepository.findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(first, LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(entry(tactics, first, 5, 10, 10), entry(tactics, first.plusDays(7), 8, 10, 12)));

        MonthlyStatsViewModel stats = service.monthStats(2026, 6);

        assertThat(stats.trainingDays()).isEqualTo(2);
        assertThat(stats.totalTasks()).isEqualTo(20);
        assertThat(stats.successRate()).isEqualTo(65);
        assertThat(stats.improvedCategories()).contains("Tactics");
    }

    private TrainingCategory category(String name, int sortOrder) {
        TrainingCategory category = new TrainingCategory();
        category.setKey(name.toLowerCase());
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
}
