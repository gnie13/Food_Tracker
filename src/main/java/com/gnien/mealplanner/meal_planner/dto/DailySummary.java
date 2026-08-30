package com.gnien.mealplanner.meal_planner.dto;

import java.time.LocalDate;
import java.util.List;

/** Nutrition totals for a single day, plus the per-meal breakdown. */
public record DailySummary(
    LocalDate date,
    NutritionTotals totals,
    List<MealResponse> meals
) {
}
