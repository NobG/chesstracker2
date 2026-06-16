package com.nobg.chesstracker2.viewmodel;

import java.time.LocalDate;

public record CategoryStatViewModel(
        String categoryName,
        String iconKey,
        boolean beta,
        int trainingUnits,
        int successCount,
        int totalCount,
        boolean challenge,
        Integer challengeBestSolved,
        Integer currentRating,
        Integer startRating,
        Integer ratingChangeSinceStart,
        String formattedRatingChangeSinceStart,
        String ratingChangeTone,
        boolean ratingCategory,
        Integer successRate,
        LocalDate lastEntryDate,
        LocalDate bestDay,
        Integer bestDayRate,
        LocalDate worstDay,
        Integer worstDayRate,
        String trend
) {
}
