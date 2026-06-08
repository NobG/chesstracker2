package com.nobg.chesstracker2.service;

final class CategoryIconMapper {

    private CategoryIconMapper() {
    }

    static String iconKeyFor(String categoryKey) {
        return switch (categoryKey) {
            case "advantage-capitalization" -> "flag";
            case "tactics" -> "target";
            case "opening-improver" -> "book-up";
            case "practice-visualization" -> "eye";
            case "blunder-preventer" -> "warning";
            case "360-trainer" -> "rotate";
            case "intuition-trainer" -> "spark";
            case "retry-mistakes" -> "retry";
            case "endgame" -> "rook";
            case "defender" -> "shield";
            case "time-trainer" -> "clock";
            case "blindfold-tactics" -> "eye-off";
            case "checkmate-patterns" -> "king";
            case "opening-trainer" -> "book";
            case "tactics-challenge" -> "bolt";
            case "advantage-conversion" -> "flag";
            case "calculation" -> "branches";
            case "visualization" -> "eye";
            case "endgames" -> "rook";
            case "openings" -> "book";
            case "strategy" -> "compass";
            case "defense" -> "shield";
            case "blunder-prevention" -> "warning";
            case "time-management" -> "clock";
            default -> "chess";
        };
    }

    static boolean isBeta(String categoryKey) {
        return "tactics-challenge".equals(categoryKey);
    }
}
