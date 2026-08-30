package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.FoodPayload;
import com.gnien.mealplanner.meal_planner.dto.NutritionTotals;
import com.gnien.mealplanner.meal_planner.model.Food;
import com.gnien.mealplanner.meal_planner.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * The local store of {@link Food} rows — shared by meal logging and recipes so
 * both dedupe foods by USDA id and feed the same usage stats.
 */
@Service
public class FoodCatalogService {

    private final FoodRepository foodRepository;

    public FoodCatalogService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    /** Reuse an already-stored food by its USDA id, otherwise persist the payload. */
    @Transactional
    public Food resolveFood(FoodPayload payload) {
        return foodRepository.findByFdcId(payload.fdcId())
            .orElseGet(() -> {
                Food food = new Food();
                food.setFdcId(payload.fdcId());
                food.setName(payload.name());
                food.setCalories(payload.calories());
                food.setProtein(payload.protein());
                food.setCarbs(payload.carbs());
                food.setFat(payload.fat());
                return foodRepository.save(food);
            });
    }

    /** Count this food as logged once more, right now — feeds the frequently-added list. */
    @Transactional
    public void recordUsage(Food food) {
        food.setTimesLogged(food.getTimesLogged() + 1);
        food.setLastLoggedAt(Instant.now());
        foodRepository.save(food);
    }

    /** The food's stored per-serving macros, unscaled and unrounded. */
    public NutritionTotals macrosOf(Food food) {
        return new NutritionTotals(
            food.getCalories(), food.getProtein(), food.getCarbs(), food.getFat());
    }
}
