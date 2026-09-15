package com.personalProject.meal_planner;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MenuController {

    // Remembers the last menu we built, and which day it was built for.
    private static List<NutritionTaggedMenuItem> cachedMenu = null;
    private static LocalDate cachedDate = null;

    @GetMapping("/api/menu")
    public List<NutritionTaggedMenuItem> getMenu() throws Exception {
        LocalDate today = LocalDate.now();

        // If we already built today's menu, just hand back the saved copy.
        if (cachedMenu != null && cachedDate != null && cachedDate.equals(today)) {
            return cachedMenu;
        }

        // Otherwise, build it fresh and remember it for next time.
        List<ScrapedMenuItem> menu = MenuScraper.scrapeMenu();
        List<NutritionTaggedMenuItem> taggedMenu = new ArrayList<>();

        for (ScrapedMenuItem item : menu) {
            DishNutritionResult result = DishNutritionCalculator.getForDish(item.getDishName());
            taggedMenu.add(new NutritionTaggedMenuItem(
                    item.getVenue(), item.getCategory(), item.getDishName(),
                    result.getTotalCalories(), result.getTotalProtein()
            ));
        }

        cachedMenu = taggedMenu;
        cachedDate = today;
        return cachedMenu;
    }
}