package com.gnien.mealplanner.meal_planner.dto;

/**
 * One ingredient of a recipe. {@code nutrition} is scaled by {@code servingSize};
 * {@code fdcId} and {@code perServing} are the raw food values, so a client can
 * round-trip the ingredient back through {@code PUT /api/recipes/{id}}.
 */
public record RecipeIngredientResponse(
    Long id,
    Long foodId,
    Long fdcId,
    String foodName,
    double servingSize,
    NutritionTotals perServing,
    NutritionTotals nutrition
) {
}
