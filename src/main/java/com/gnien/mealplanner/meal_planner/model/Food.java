package com.gnien.mealplanner.meal_planner.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
}