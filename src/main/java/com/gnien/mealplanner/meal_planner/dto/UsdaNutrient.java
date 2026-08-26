package com.gnien.mealplanner.meal_planner.dto;

public class UsdaNutrient {
    private String nutrientName;
    private double value;

    public String getNutrientName() { return nutrientName; }
    public void setNutrientName(String nutrientName) { this.nutrientName = nutrientName; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}