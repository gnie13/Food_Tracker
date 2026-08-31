package com.gnien.mealplanner.meal_planner.dto;

import jakarta.validation.constraints.Positive;

/** Request body for editing a logged entry's serving-size multiplier in place. */
public record UpdateEntryRequest(
    @Positive double servingSize
) {
}
