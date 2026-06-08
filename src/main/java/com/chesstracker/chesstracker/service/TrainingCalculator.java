package com.chesstracker.chesstracker.service;

import com.chesstracker.chesstracker.model.DailyTrainingEntry;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TrainingCalculator {

    private static final Pattern RESULT_PATTERN = Pattern.compile("^\\s*(\\d+)\\s*/\\s*(\\d+)\\s*$");

    private TrainingCalculator() {
    }

    public static TrainingResult parseResult(String value) {
        if (value == null || value.isBlank()) {
            return new TrainingResult(0, 0);
        }
        Matcher matcher = RESULT_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Ergebnis muss im Format 7/10 eingegeben werden.");
        }
        int success = Integer.parseInt(matcher.group(1));
        int total = Integer.parseInt(matcher.group(2));
        if (success > total) {
            throw new IllegalArgumentException("Erfolg darf nicht groesser als die Gesamtzahl sein.");
        }
        return new TrainingResult(success, total);
    }

    public static Integer successRate(int successCount, int totalCount) {
        if (totalCount <= 0) {
            return null;
        }
        return Math.round((successCount * 100f) / totalCount);
    }

    public static int totalSuccess(Collection<DailyTrainingEntry> entries) {
        return entries.stream().filter(DailyTrainingEntry::isTrained).mapToInt(DailyTrainingEntry::getSuccessCount).sum();
    }

    public static int totalTasks(Collection<DailyTrainingEntry> entries) {
        return entries.stream().filter(DailyTrainingEntry::isTrained).mapToInt(DailyTrainingEntry::getTotalCount).sum();
    }

    public static int totalDuration(Collection<DailyTrainingEntry> entries) {
        return entries.stream()
                .filter(DailyTrainingEntry::isTrained)
                .map(DailyTrainingEntry::getDurationMinutes)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
