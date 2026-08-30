package com.gnien.mealplanner.meal_planner.dto;

import com.gnien.mealplanner.meal_planner.model.Meal;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Partial update for a logged entry — every field is optional. A non-null
 * {@code date} or {@code mealType} re-homes the entry to that meal (created if
 * it doesn't exist yet), leaving its old meal to be cleaned up if it ends up empty.
 */
public record UpdateEntryRequest(
    @Positive Double servingSize,
    LocalDate date,
    Meal.MealType mealType
) {
    /** True when the payload carries nothing to apply. */
    public boolean isEmpty() {
        return servingSize == null && date == null && mealType == null;
    }
}
