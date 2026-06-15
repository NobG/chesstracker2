package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.TrainingCategory;

public final class TrainingCategoryRules {

    public static final String TACTICS_CHALLENGE_KEY = "tactics-challenge";

    private TrainingCategoryRules() {
    }

    public static boolean isPointsOnlyCategory(TrainingCategory category) {
        return category != null && TACTICS_CHALLENGE_KEY.equals(category.getKey());
    }

    public static boolean isPointsOnlyEntry(DailyTrainingEntry entry) {
        return entry != null && isPointsOnlyCategory(entry.getCategory());
    }

    public static Integer pointsValue(DailyTrainingEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.getScore() != null) {
            return entry.getScore();
        }
        return entry.getSuccessCount() > 0 ? entry.getSuccessCount() : null;
    }
}
