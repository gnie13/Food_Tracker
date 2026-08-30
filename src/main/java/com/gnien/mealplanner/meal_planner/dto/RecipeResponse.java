package com.gnien.mealplanner.meal_planner.dto;

import java.util.List;

/** A stored recipe, its ingredients, and the macro totals for one full batch. */
public record RecipeResponse(
    Long id,
    String name,
    List<RecipeIngredientResponse> ingredients,
    NutritionTotals totals
) {
}
