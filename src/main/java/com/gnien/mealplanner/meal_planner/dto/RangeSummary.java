package com.gnien.mealplanner.meal_planner.dto;

import java.time.LocalDate;
import java.util.List;

/** Nutrition totals across a date range (inclusive), with one {@link DailySummary} per day. */
public record RangeSummary(
    LocalDate startDate,
    LocalDate endDate,
    NutritionTotals totals,
    List<DailySummary> days
) {
}
