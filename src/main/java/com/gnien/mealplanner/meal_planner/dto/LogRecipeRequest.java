package com.gnien.mealplanner.meal_planner.dto;

import com.gnien.mealplanner.meal_planner.model.Meal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Log a whole recipe against a meal. {@code factor} scales every ingredient's
 * serving size — 0.5 logs half a batch, 2 logs a double batch.
 */
public record LogRecipeRequest(
    @NotNull LocalDate date,
    @NotNull Meal.MealType mealType,
    @NotNull Long recipeId,
    @Positive double factor
) {
}
