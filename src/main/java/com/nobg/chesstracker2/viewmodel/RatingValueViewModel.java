package com.nobg.chesstracker2.viewmodel;

public record RatingValueViewModel(
        String label,
        Integer value,
        Integer change,
        String formattedChange,
        String changeTone
) {

    public static RatingValueViewModel of(String label, Integer value, Integer previousValue) {
        if (value == null) {
            return new RatingValueViewModel(label, null, null, "-", "none");
        }
        if (previousValue == null) {
            return new RatingValueViewModel(label, value, null, "neu", "new");
        }
        int difference = value - previousValue;
        String formatted = difference == 0 ? "+/-0" : difference > 0 ? "+" + difference : String.valueOf(difference);
        String tone = difference > 0 ? "positive" : difference < 0 ? "negative" : "neutral";
        return new RatingValueViewModel(label, value, difference, formatted, tone);
    }
}
