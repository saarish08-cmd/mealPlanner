package com.personalProject.meal_planner;
import java.util.List;

public class DishNutritionCalculator {

    public static void main(String[] args) throws Exception {
        calculate("Cajun Shrimp Pasta");
    }

    // Calculates total calories/protein for one dish, ingredient by ingredient.
    public static void calculate(String dishName) throws Exception {
        List<IngredientEstimate> ingredients = DishDecomposer.decompose(dishName);

        double totalCalories = 0;
        double totalProtein = 0;
        double totalGrams = 0;

        for (IngredientEstimate ingredient : ingredients) {
            NutritionMatch match = NutritionApi.matchIngredient(ingredient.getName());

            if (match == null) {
                System.out.println("No match for: " + ingredient.getName() + " (skipped)");
                continue;
            }

            // USDA values are per 100g, so scale by how many grams we actually have.
            double scale = ingredient.getGrams() / 100.0;
            totalCalories += match.getCaloriesPer100g() * scale;
            totalProtein += match.getProteinPer100g() * scale;
            totalGrams += ingredient.getGrams();
            System.out.println(ingredient.getName() + " -> " + match.getDescription() + " (" + ingredient.getGrams() + "g)");
        }

        System.out.println(dishName + " TOTAL: " + totalCalories + " kcal, " + totalProtein + "g protein, " + totalGrams + " g total weight");
    }
}