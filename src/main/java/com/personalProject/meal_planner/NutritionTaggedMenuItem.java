package com.personalProject.meal_planner;

public class NutritionTaggedMenuItem {
    //Private Variables
    private String venue;
    private String category;
    private String dishName;
    private double calories;
    private double protein;

    //Constructor
    public NutritionTaggedMenuItem(String venue, String category, String dishName, double calories, double protein) {
        this.venue = venue;
        this.category = category;
        this.dishName = dishName;
        this.calories = calories;
        this.protein = protein;
    }

    //Getters
    public String getVenue() { return venue; }
    public String getCategory() { return category; }
    public String getDishName() { return dishName; }
    public double getCalories() { return calories; }
    public double getProtein() { return protein; }

    @Override
    public String toString() {
        return venue + " / " + category + " / " + dishName + " — " + calories + " kcal, " + protein + "g protein";
    }
}