package com.nobg.chesstracker2.viewmodel;

public record RatingChangeViewModel(
        String label,
        int difference,
        String displayDifference
) {
}
