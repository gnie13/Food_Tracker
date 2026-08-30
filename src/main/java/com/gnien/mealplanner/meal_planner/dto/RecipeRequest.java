package com.gnien.mealplanner.meal_planner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Create or replace a recipe: a name plus at least one ingredient. */
public record RecipeRequest(
    @NotBlank String name,
    @NotEmpty @Valid List<RecipeIngredientRequest> ingredients
) {
}
