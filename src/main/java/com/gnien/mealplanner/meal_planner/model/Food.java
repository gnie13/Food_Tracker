package com.gnien.mealplanner.meal_planner.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fdcId;        // USDA's food ID, so we don't re-fetch duplicates
    private String name;

    // Macros are per 100 g — the basis USDA's search endpoint reports for every food.
    // A logged entry's servingSize multiplier is therefore "hundreds of grams".
    private double calories;
    private double protein;    // grams
    private double carbs;      // grams
    private double fat;        // grams

    // One USDA "serving" in grams (branded foods only), plus its household label
    // ("1 cup", "2 tbsp"). Null when the API gives no weight-based serving.
    private Double servingGrams;
    private String servingText;

    // Usage stats, bumped every time this food is logged — drives the "frequently added" list.
    // The column default backfills rows that predate this field (ddl-auto=update, no migrations),
    // so the primitive getter never sees a NULL.
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int timesLogged;
    private Instant lastLoggedAt;

    // Explicitly pinned by the user for quick re-adding, independent of how often it's logged.
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean saved;
}