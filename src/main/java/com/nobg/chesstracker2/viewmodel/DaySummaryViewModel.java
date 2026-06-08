package com.nobg.chesstracker2.viewmodel;

import com.nobg.chesstracker2.model.DailyCompletionStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
        DailyCompletionStatus completionStatus,
        String completionStatusLabel,
        boolean locked,
        OffsetDateTime completedAt,
        String automaticSummary,
        String copyBlock
) {
}
