package com.personalProject.meal_planner;

public class ScrapedMenuItem {
    private String venue;
    private String category;
    private String dishName;

    //Custom Constructor
    public ScrapedMenuItem(String venue, String category, String dishName) {
        this.venue = venue;
        this.category = category;
        this.dishName = dishName;
    }

    //Getters
    public String getVenue() {return venue;}
    public String getCategory() {return category;}
    public String getDishName() {return dishName;}
}
