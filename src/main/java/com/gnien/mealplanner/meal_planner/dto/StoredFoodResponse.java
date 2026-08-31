package com.gnien.mealplanner.meal_planner.dto;

import java.time.Instant;

/**
 * A food already stored locally, with its usage stats and saved flag. Used by
 * both the "frequently added" and "saved" quick-add lists. Macro fields are per
 * 100 g and mirror {@link FoodPayload} so the client can hand it straight back
 * to {@code POST /api/meals/entries}. When a meal type is in play, {@code
 * timesLogged}/{@code lastLoggedAt} reflect that meal only.
 */
public record StoredFoodResponse(
    Long foodId,
    Long fdcId,
    String name,
    double calories,
    double protein,
    double carbs,
    double fat,
    Double servingGrams,
    String servingText,
    int timesLogged,
    Instant lastLoggedAt,
    boolean saved
) {
}
