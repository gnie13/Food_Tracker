package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.UsdaFood;
import com.gnien.mealplanner.meal_planner.dto.UsdaNutrient;
import com.gnien.mealplanner.meal_planner.dto.UsdaSearchResponse;
import com.gnien.mealplanner.meal_planner.model.Food;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsdaFoodService {

    private final RestTemplate restTemplate;

    @Value("${usda.api.key}")
    private String apiKey;

    private static final String SEARCH_URL =
        "https://api.nal.usda.gov/fdc/v1/foods/search?query={query}&api_key={apiKey}";

    public UsdaFoodService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Food> searchFoods(String query) {
        UsdaSearchResponse response = restTemplate.getForObject(
            SEARCH_URL, UsdaSearchResponse.class, query, apiKey
        );

        if (response == null || response.getFoods() == null) {
            return List.of();
        }

        return response.getFoods().stream()
            .map(this::mapToFood)
            .collect(Collectors.toList());
    }

    private Food mapToFood(UsdaFood usdaFood) {
        Food food = new Food();
        food.setFdcId(usdaFood.getFdcId());
        food.setName(usdaFood.getDescription());

        List<UsdaNutrient> nutrients = usdaFood.getFoodNutrients();
        if (nutrients != null) {
            food.setCalories(findNutrient(nutrients, "Energy"));
            food.setProtein(findNutrient(nutrients, "Protein"));
            food.setCarbs(findNutrient(nutrients, "Carbohydrate, by difference"));
            food.setFat(findNutrient(nutrients, "Total lipid (fat)"));
        }

        return food;
    }

    private double findNutrient(List<UsdaNutrient> nutrients, String name) {
        return nutrients.stream()
            .filter(n -> name.equals(n.getNutrientName()))
            .map(UsdaNutrient::getValue)
            .findFirst()
            .orElse(0.0);
    }
}