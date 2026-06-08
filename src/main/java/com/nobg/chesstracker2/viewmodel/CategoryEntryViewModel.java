package com.nobg.chesstracker2.viewmodel;

public record CategoryEntryViewModel(
        Long categoryId,
        String categoryName,
        String description,
        boolean trained,
        String result,
        Integer score,
        Integer durationMinutes,
        String note,
        Integer successRate
) {
}
