package com.nobg.chesstracker2.viewmodel;

import com.nobg.chesstracker2.dto.TrainingDayForm;
import java.time.LocalDate;
import java.util.List;

public record TodayViewModel(
        LocalDate date,
        TrainingDayForm form,
        List<CategoryEntryViewModel> entries,
        DaySummaryViewModel summary
) {
}
