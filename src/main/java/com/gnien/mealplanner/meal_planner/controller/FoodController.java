package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.model.Food;
import com.gnien.mealplanner.meal_planner.service.UsdaFoodService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FoodController {

    private final UsdaFoodService usdaFoodService;

    public FoodController(UsdaFoodService usdaFoodService) {
        this.usdaFoodService = usdaFoodService;
    }

    @GetMapping("/api/foods/search")
    public List<Food> searchFoods(@RequestParam String query) {
        return usdaFoodService.searchFoods(query);
    }
}