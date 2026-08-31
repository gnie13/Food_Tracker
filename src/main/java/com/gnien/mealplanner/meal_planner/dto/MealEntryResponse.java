package com.gnien.mealplanner.meal_planner.dto;

/**
 * One logged food within a meal, with its macros already scaled by serving size.
 * {@code grams} is the weight this entry represents (servingSize × 100, since
 * macros are per 100 g); {@code servingGrams}/{@code servingText} echo the food's
 * USDA serving hint so a client can offer a "1 serving" quantity option.
 */
public record MealEntryResponse(
    Long id,
    Long foodId,
    String foodName,
    double servingSize,
    double grams,
    Double servingGrams,
    String servingText,
    NutritionTotals nutrition
) {
}
