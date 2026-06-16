package com.nobg.chesstracker2.viewmodel;

import java.util.List;

public record RatingSummaryViewModel(
        List<RatingValueViewModel> manualRatings,
        List<RatingValueViewModel> aimchessRatings
) {

    public boolean hasAnyRating() {
        return !manualRatings.isEmpty() || !aimchessRatings.isEmpty();
    }
}
