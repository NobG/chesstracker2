package com.nobg.chesstracker2.viewmodel;

import com.nobg.chesstracker2.model.WeeklyGoalClosureStatus;

public record WeeklyGoalClosureViewModel(
        int isoYear,
        int isoWeek,
        WeeklyGoalClosureStatus status,
        String displayText,
        String tone,
        String note
) {
}
