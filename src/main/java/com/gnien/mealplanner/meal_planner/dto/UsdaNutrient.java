package com.gnien.mealplanner.meal_planner.dto;

public class UsdaNutrient {
    private String nutrientName;
    private String unitName;
    private double value;

    public String getNutrientName() { return nutrientName; }
    public void setNutrientName(String nutrientName) { this.nutrientName = nutrientName; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
