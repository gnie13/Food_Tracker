package com.gnien.mealplanner.meal_planner.dto;

import com.gnien.mealplanner.meal_planner.model.Meal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/** Request body for logging one food against a meal on a given date. */
public record LogEntryRequest(
    @NotNull LocalDate date,
    @NotNull Meal.MealType mealType,
    @NotNull @Valid FoodPayload food,
    @Positive double servingSize
) {
}
