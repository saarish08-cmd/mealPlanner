package com.personalProject.meal_planner;
import java.io.IOException;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MenuScraper {
    public static void main(String[] args) throws IOException {
        scrapeMenu().forEach(System.out::println);
    }

    // Pulls today's real menu from the site and returns it as a list.
    // Other files can call this instead of copying the scraping logic.
    public static List<ScrapedMenuItem> scrapeMenu() throws IOException {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = "https://uwaterloo.ca/food-services-information/content/daily-menu-" + today; // update the date as needed

        Document doc = Jsoup.connect(url).get();
        Elements locations = doc.select("div.card__food-location");
        List<ScrapedMenuItem> items = new ArrayList<>();

        for (Element venue : locations) {
            String mealName = venue.selectFirst("h2").text();
            String currentSection = "";

            for (Element child : venue.children()) {
                if (child.tagName().equals("h3")) currentSection = child.text();
                else if (child.hasClass("food_item")) {
                    String dishName = child.selectFirst("a.food_link").text();
                    items.add(new ScrapedMenuItem(mealName, currentSection, dishName));
                }
            }
        }
        return items;
    }
}