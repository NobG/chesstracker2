package com.nobg.chesstracker2.viewmodel;

import java.util.List;

public record MonthlyStatsViewModel(
        int year,
        int month,
        int daysInMonth,
        int trainingDays,
        Integer trainingDayPercentage,
        int totalDurationMinutes,
        int totalTasks,
        Integer successRate,
        List<CategoryStatViewModel> categories,
        List<String> improvedCategories,
        List<String> declinedCategories,
        String summary
) {
}
