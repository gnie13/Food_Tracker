package com.gnien.mealplanner.meal_planner.dto;

import com.gnien.mealplanner.meal_planner.model.Meal;

import java.time.LocalDate;
import java.util.List;

/** A meal (breakfast/lunch/dinner/snack) on a date, its entries, and their subtotal. */
public record MealResponse(
    Long id,
    LocalDate date,
    Meal.MealType mealType,
    List<MealEntryResponse> entries,
    NutritionTotals subtotal
) {
}
