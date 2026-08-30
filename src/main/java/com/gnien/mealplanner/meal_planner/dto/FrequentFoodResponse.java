package com.gnien.mealplanner.meal_planner.dto;

import java.time.Instant;

/**
 * A previously-logged food plus how often and how recently it was used, for the
 * "frequently added" quick-add list. Macro fields mirror {@link FoodPayload} so
 * the client can hand it straight back to {@code POST /api/meals/entries}.
 */
public record FrequentFoodResponse(
    Long foodId,
    Long fdcId,
    String name,
    double calories,
    double protein,
    double carbs,
    double fat,
    int timesLogged,
    Instant lastLoggedAt
) {
}
