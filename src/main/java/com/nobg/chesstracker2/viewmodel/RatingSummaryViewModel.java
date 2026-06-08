package com.nobg.chesstracker2.viewmodel;

import java.time.LocalDate;

public record RatingSummaryViewModel(
        LocalDate snapshotDate,
        Integer lichessBlitz,
        Integer lichessRapid,
        Integer lichessClassical,
        Integer dwz,
        Integer fideElo,
        boolean hasAnyRating
) {
}
