package com.personalProject.meal_planner;

public class DishNutritionResult {
    private double totalCalories;
    private double totalProtein;
    private double totalGrams;

    public DishNutritionResult(double totalCalories, double totalProtein, double totalGrams) {
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.totalGrams = totalGrams;
    }

    public double getTotalCalories() { return totalCalories; }
    public double getTotalProtein() { return totalProtein; }
    public double getTotalGrams() { return totalGrams; }

    @Override
    public String toString() {
        return totalCalories + " kcal, " + totalProtein + "g protein, " + totalGrams + "g total";
    }
}