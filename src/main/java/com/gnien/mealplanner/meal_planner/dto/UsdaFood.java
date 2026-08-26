package com.gnien.mealplanner.meal_planner.dto;

import java.util.List;

public class UsdaFood {
    private Long fdcId;
    private String description;
    private List<UsdaNutrient> foodNutrients;

    public Long getFdcId() { return fdcId; }
    public void setFdcId(Long fdcId) { this.fdcId = fdcId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<UsdaNutrient> getFoodNutrients() { return foodNutrients; }
    public void setFoodNutrients(List<UsdaNutrient> foodNutrients) { this.foodNutrients = foodNutrients; }
}