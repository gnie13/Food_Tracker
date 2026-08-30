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

    private double calories;   // per serving
    private double protein;    // grams
    private double carbs;      // grams
    private double fat;        // grams

    // Usage stats, bumped every time this food is logged — drives the "frequently added" list.
    // The column default backfills rows that predate this field (ddl-auto=update, no migrations),
    // so the primitive getter never sees a NULL.
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int timesLogged;
    private Instant lastLoggedAt;
}