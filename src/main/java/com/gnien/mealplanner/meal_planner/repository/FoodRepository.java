package com.gnien.mealplanner.meal_planner.repository;

import com.gnien.mealplanner.meal_planner.model.Food;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {
    Optional<Food> findByFdcId(Long fdcId);

    /** Most-logged foods first, breaking ties by most recently logged. */
    List<Food> findByTimesLoggedGreaterThanOrderByTimesLoggedDescLastLoggedAtDesc(
        int threshold, Pageable pageable);
}
