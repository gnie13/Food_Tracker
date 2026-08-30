package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.FrequentFoodResponse;
import com.gnien.mealplanner.meal_planner.model.Food;
import com.gnien.mealplanner.meal_planner.service.MealService;
import com.gnien.mealplanner.meal_planner.service.UsdaFoodService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FoodController {

    private final UsdaFoodService usdaFoodService;
    private final MealService mealService;

    public FoodController(UsdaFoodService usdaFoodService, MealService mealService) {
        this.usdaFoodService = usdaFoodService;
        this.mealService = mealService;
    }

    @GetMapping("/api/foods/search")
    public List<Food> searchFoods(@RequestParam String query) {
        return usdaFoodService.searchFoods(query);
    }

    /** Foods logged most often, for one-tap re-adding. Defaults to the top 10. */
    @GetMapping("/api/foods/frequent")
    public List<FrequentFoodResponse> frequentFoods(
        @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return mealService.frequentFoods(limit);
    }
}
