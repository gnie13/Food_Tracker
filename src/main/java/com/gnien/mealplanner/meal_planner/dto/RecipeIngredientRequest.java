package com.gnien.mealplanner.meal_planner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** One ingredient in a {@link RecipeRequest} — a food and its base serving size. */
public record RecipeIngredientRequest(
    @NotNull @Valid FoodPayload food,
    @Positive double servingSize
) {
}
