package com.gnien.mealplanner.meal_planner.dto;

/**
 * A bundle of the four macros we track. Immutable; the helper methods return
 * new instances so it's safe to use in streams.
 */
public record NutritionTotals(double calories, double protein, double carbs, double fat) {

    public static NutritionTotals zero() {
        return new NutritionTotals(0, 0, 0, 0);
    }

    public NutritionTotals plus(NutritionTotals other) {
        return new NutritionTotals(
            calories + other.calories,
            protein + other.protein,
            carbs + other.carbs,
            fat + other.fat
        );
    }

    public NutritionTotals scale(double factor) {
        return new NutritionTotals(
            calories * factor,
            protein * factor,
            carbs * factor,
            fat * factor
        );
    }

    /** Round to one decimal so the API doesn't return 41.900000000000006. */
    public NutritionTotals rounded() {
        return new NutritionTotals(round(calories), round(protein), round(carbs), round(fat));
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
