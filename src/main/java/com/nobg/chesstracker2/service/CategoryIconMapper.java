package com.nobg.chesstracker2.service;

final class CategoryIconMapper {

    private CategoryIconMapper() {
    }

    static String iconKeyFor(String categoryKey) {
        return switch (categoryKey) {
            case "tactics" -> "target";
            case "calculation" -> "branches";
            case "visualization" -> "eye";
            case "endgames" -> "rook";
            case "openings" -> "book";
            case "strategy" -> "compass";
            case "defense" -> "shield";
            case "blunder-prevention" -> "warning";
            case "time-management" -> "clock";
            case "advantage-conversion" -> "flag";
            default -> "chess";
        };
    }
}
