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

    public void setName(String name) {
        this.name = name;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
