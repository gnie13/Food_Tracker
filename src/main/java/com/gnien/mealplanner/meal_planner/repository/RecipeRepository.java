package com.gnien.mealplanner.meal_planner.repository;

import com.gnien.mealplanner.meal_planner.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findAllByOrderByNameAsc();
}
