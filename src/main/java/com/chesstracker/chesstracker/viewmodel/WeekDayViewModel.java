package com.chesstracker.chesstracker.viewmodel;

import java.time.LocalDate;

public record WeekDayViewModel(
        LocalDate date,
        int trainedCategories,
        int totalTasks,
        Integer successRate,
        int durationMinutes
) {
}
