package com.nobg.chesstracker2.viewmodel;

import java.time.LocalDate;
import java.util.List;

public record DaySummaryViewModel(
        LocalDate date,
        List<CategoryEntryViewModel> trainedEntries,
        int trainedCategoryCount,
        int successCount,
        int totalCount,
        Integer successRate,
        int totalDurationMinutes,
        String dayNote,
        String automaticSummary,
        String copyBlock
) {
}
