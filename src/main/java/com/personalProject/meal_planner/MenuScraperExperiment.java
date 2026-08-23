package com.personalProject.meal_planner;
import java.io.IOException;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.lang.String;

public class MenuScraperExperiment {
    public static void main(String[] args) throws IOException{
        String url = "https://uwaterloo.ca/food-services/daily-menu?date=2026-09-07";
        Document doc = Jsoup.connect(url).get();
        Elements titles = doc.select("a.food_link");
        for(var titleElement : titles){
            System.out.println(titleElement.text());
        }
    }
}
