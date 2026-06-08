package com.chesstracker.chesstracker.viewmodel;

import java.time.LocalDate;

public record CategoryStatViewModel(
        String categoryName,
        int trainingUnits,
        int successCount,
        int totalCount,
        Integer successRate,
        LocalDate lastEntryDate,
        LocalDate bestDay,
        Integer bestDayRate,
        LocalDate worstDay,
        Integer worstDayRate,
        String trend
) {
}
