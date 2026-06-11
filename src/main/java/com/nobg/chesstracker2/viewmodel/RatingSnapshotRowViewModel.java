package com.nobg.chesstracker2.viewmodel;

import java.time.LocalDate;

public record RatingSnapshotRowViewModel(
        LocalDate snapshotDate,
        Integer lichessBlitz,
        Integer lichessRapid,
        Integer lichessClassical,
        Integer dwz,
        Integer fideElo,
        Integer tacticsRating,
        Integer endgameRating,
        String note
) {
}
