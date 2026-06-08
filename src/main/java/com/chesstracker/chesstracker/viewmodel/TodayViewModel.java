package com.chesstracker.chesstracker.viewmodel;

import com.chesstracker.chesstracker.dto.TrainingDayForm;
import java.time.LocalDate;
import java.util.List;

public record TodayViewModel(
        LocalDate date,
        TrainingDayForm form,
        List<CategoryEntryViewModel> entries,
        DaySummaryViewModel summary
) {
}
