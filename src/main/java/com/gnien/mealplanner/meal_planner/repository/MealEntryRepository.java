package com.gnien.mealplanner.meal_planner.repository;

import com.gnien.mealplanner.meal_planner.model.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {
}