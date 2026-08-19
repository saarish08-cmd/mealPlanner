package com.personalProject.meal_planner;

public class MealItem {
    private String name;
    private double calories;
    private double protein;
    private String unit; //Ex: cup, scoop, pieces

    public MealItem(String name, double calories, double protein, String unit) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public double getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public String getUnit() {return unit;}
}
