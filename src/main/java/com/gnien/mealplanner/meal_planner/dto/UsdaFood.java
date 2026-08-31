package com.gnien.mealplanner.meal_planner.dto;

import java.util.List;

public class UsdaFood {
    private Long fdcId;
    private String description;
    private String dataType;
    private Double servingSize;
    private String servingSizeUnit;
    private String householdServingFullText;
    private List<UsdaNutrient> foodNutrients;

    public Long getFdcId() { return fdcId; }
    public void setFdcId(Long fdcId) { this.fdcId = fdcId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public Double getServingSize() { return servingSize; }
    public void setServingSize(Double servingSize) { this.servingSize = servingSize; }
    public String getServingSizeUnit() { return servingSizeUnit; }
    public void setServingSizeUnit(String servingSizeUnit) { this.servingSizeUnit = servingSizeUnit; }
    public String getHouseholdServingFullText() { return householdServingFullText; }
    public void setHouseholdServingFullText(String s) { this.householdServingFullText = s; }
    public List<UsdaNutrient> getFoodNutrients() { return foodNutrients; }
    public void setFoodNutrients(List<UsdaNutrient> foodNutrients) { this.foodNutrients = foodNutrients; }
}
