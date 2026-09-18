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

    public static List<ScrapedMenuItem> scrapeMenu() throws IOException {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        //Test if the issue is truly in the scraper or a helper class
        String url = "https://uwaterloo.ca/food-services/daily-menu?date=" + today;
        System.out.println("Fetched: " + url);

        Document doc = Jsoup.connect(url).get();
        System.out.println("Page title: " + doc.title());

        Elements locations = doc.select("div.card__food-location");
        System.out.println("Venues found: " + locations.size());

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