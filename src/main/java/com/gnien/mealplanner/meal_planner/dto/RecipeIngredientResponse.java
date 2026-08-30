package com.gnien.mealplanner.meal_planner.dto;

/** One ingredient of a recipe, with its macros already scaled by the base serving size. */
public record RecipeIngredientResponse(
    Long id,
    Long foodId,
    String foodName,
    double servingSize,
    NutritionTotals nutrition
) {
}
