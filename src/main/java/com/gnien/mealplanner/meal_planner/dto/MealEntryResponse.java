package com.gnien.mealplanner.meal_planner.dto;

/** One logged food within a meal, with its macros already scaled by serving size. */
public record MealEntryResponse(
    Long id,
    Long foodId,
    String foodName,
    double servingSize,
    NutritionTotals nutrition
) {
}
