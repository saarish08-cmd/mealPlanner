package com.personalProject.meal_planner;

import java.util.ArrayList;
import java.util.List;

public class MenuNutritionService {

    public static void main(String[] args) throws Exception {
        List<ScrapedMenuItem> menu = MenuScraper.scrapeMenu();
        List<NutritionTaggedMenuItem> taggedMenu = new ArrayList<>();

        for (ScrapedMenuItem item : menu) {
            DishNutritionResult result = DishNutritionCalculator.getForDish(item.getDishName());
            taggedMenu.add(new NutritionTaggedMenuItem(
                    item.getVenue(),
                    item.getCategory(),
                    item.getDishName(),
                    result.getTotalCalories(),
                    result.getTotalProtein()
            ));
        }

        taggedMenu.forEach(System.out::println);
    }
}