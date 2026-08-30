package com.personalProject.meal_planner;

public class IngredientEstimate {
    private String name;
    private double grams;

    public IngredientEstimate(String name, double grams) {
        this.name = name;
        this.grams = grams;
    }

    public String getName() { return name; }
    public double getGrams() { return grams; }

    @Override
    public String toString() {
        return name + ": " + grams + "g";
    }
}