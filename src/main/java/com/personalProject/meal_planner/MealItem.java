package com.personalProject.meal_planner;

public class MealItem {
    private String name;
    private double calories;
    private double protein;

    public MealItem(String name, double calories, double protein) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
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
}
