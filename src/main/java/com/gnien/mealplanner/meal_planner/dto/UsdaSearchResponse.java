package com.gnien.mealplanner.meal_planner.dto;

import java.util.List;

public class UsdaSearchResponse {
    private List<UsdaFood> foods;

    public List<UsdaFood> getFoods() { return foods; }
    public void setFoods(List<UsdaFood> foods) { this.foods = foods; }
}