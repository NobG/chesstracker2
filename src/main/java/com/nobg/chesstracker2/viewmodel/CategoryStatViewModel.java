package com.nobg.chesstracker2.viewmodel;

import java.time.LocalDate;

public record CategoryStatViewModel(
        String categoryName,
        String iconKey,
        boolean beta,
        int trainingUnits,
        int successCount,
        int totalCount,
        Integer currentRating,
        Integer successRate,
        LocalDate lastEntryDate,
        LocalDate bestDay,
        Integer bestDayRate,
        LocalDate worstDay,
        Integer worstDayRate,
        String trend
) {
}
