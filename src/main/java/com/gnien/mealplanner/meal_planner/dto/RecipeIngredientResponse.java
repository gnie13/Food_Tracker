package com.gnien.mealplanner.meal_planner.dto;

/**
 * One ingredient of a recipe. {@code nutrition} is scaled by {@code servingSize};
 * {@code fdcId} and {@code perServing} (per 100 g) let a client round-trip the
 * ingredient back through {@code PUT /api/recipes/{id}}. {@code grams} is the
 * weight the ingredient represents; {@code servingGrams}/{@code servingText}
 * echo the food's USDA serving hint.
 */
public record RecipeIngredientResponse(
    Long id,
    Long foodId,
    Long fdcId,
    String foodName,
    double servingSize,
    double grams,
    Double servingGrams,
    String servingText,
    NutritionTotals perServing,
    NutritionTotals nutrition
) {
}
