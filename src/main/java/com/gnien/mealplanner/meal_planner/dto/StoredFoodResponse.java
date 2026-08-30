package com.gnien.mealplanner.meal_planner.dto;

import java.time.Instant;

/**
 * A food already stored locally, with its usage stats and saved flag. Used by
 * both the "frequently added" and "saved" quick-add lists. Macro fields mirror
 * {@link FoodPayload} so the client can hand it straight back to
 * {@code POST /api/meals/entries}.
 */
public record StoredFoodResponse(
    Long foodId,
    Long fdcId,
    String name,
    double calories,
    double protein,
    double carbs,
    double fat,
    int timesLogged,
    Instant lastLoggedAt,
    boolean saved
) {
}
