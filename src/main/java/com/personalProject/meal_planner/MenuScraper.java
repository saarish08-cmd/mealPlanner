package com.personalProject.meal_planner;
import java.io.IOException;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

public class MenuScraper {
    public static void main(String[] args) throws IOException{
        String url = "https://uwaterloo.ca/food-services/daily-menu?date=2026-09-07";
        Document doc = Jsoup.connect(url).get();
        Elements locations = doc.select("div.card__food-location");
        List<ScrapedMenuItem> items = new ArrayList<>(); //Empty list to collect results

        for(Element venue : locations){
            String mealName = venue.selectFirst("h2").text();
            String currentSection = "";

            for(Element child : venue.children()){
                if(child.tagName().equals("h3"))
                    currentSection = child.text();
                else if(child.hasClass("food_item")) {
                    String dishName = child.selectFirst("a.food_link").text();
                    items.add(new ScrapedMenuItem(mealName, currentSection, dishName));
                }
            }
        }

        //Print
        items.forEach(System.out::println);
    }
}
