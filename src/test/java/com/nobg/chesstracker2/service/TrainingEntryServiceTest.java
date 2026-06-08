package com.nobg.chesstracker2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyNoteRepository;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.viewmodel.DaySummaryViewModel;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

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
}
