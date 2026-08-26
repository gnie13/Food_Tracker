package com.gnien.mealplanner.meal_planner.repository;

import com.gnien.mealplanner.meal_planner.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByDate(LocalDate date);
}