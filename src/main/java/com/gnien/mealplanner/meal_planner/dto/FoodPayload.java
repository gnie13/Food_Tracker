package com.gnien.mealplanner.meal_planner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * The food fields a client sends back when logging an entry — normally copied
 * straight from a {@code /api/foods/search} result, so we don't have to re-hit
 * the USDA API just to persist it.
 */
public record FoodPayload(
    @NotNull Long fdcId,
    @NotBlank String name,
    @NotNull @PositiveOrZero Double calories,
    @NotNull @PositiveOrZero Double protein,
    @NotNull @PositiveOrZero Double carbs,
    @NotNull @PositiveOrZero Double fat
) {
}
