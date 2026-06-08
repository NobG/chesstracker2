package com.nobg.chesstracker2.viewmodel;

import java.time.LocalDate;
import java.util.List;

public record WeeklyStatsViewModel(
        int year,
        int week,
        LocalDate startDate,
        LocalDate endDate,
        List<WeekDayViewModel> days,
        List<CategoryStatViewModel> categories,
        int trainingDays,
        int trainedCategories,
        int totalTasks,
        int totalDurationMinutes,
        Integer successRate,
        int completedTrainingDays,
        int partialTrainingDays,
        int openTrainingDaysWithEntries,
        String bestCategory,
        String weakestCategory,
        String summary
) {
}
