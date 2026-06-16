package com.nobg.chesstracker2.viewmodel;

public record RatingValueViewModel(
        String label,
        Integer value,
        Integer change,
        String formattedChange,
        String changeTone
) {

    public static RatingValueViewModel latest(String label, Integer value) {
        return new RatingValueViewModel(label, value, null, "-", "none");
    }

    public static RatingValueViewModel period(String label, Integer latestValue, Integer periodValue, Integer previousValue, String changeSuffix) {
        if (periodValue == null) {
            return latest(label, latestValue);
        }
        if (previousValue == null) {
            return new RatingValueViewModel(label, periodValue, null, "neu", "new");
        }
        int difference = periodValue - previousValue;
        String formatted = formatDifference(difference) + " " + changeSuffix;
        String tone = difference > 0 ? "positive" : difference < 0 ? "negative" : "neutral";
        return new RatingValueViewModel(label, periodValue, difference, formatted, tone);
    }

    private static String formatDifference(int difference) {
        return difference == 0 ? "+/-0" : difference > 0 ? "+" + difference : String.valueOf(difference);
    }
}
