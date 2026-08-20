package com.personalProject.meal_planner;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity //Marks a Java class to automatically map its fields to a database table
public class Profile {
    @Id //Marks a class field as the primary key for the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Tells database to automatically create unique values for the primary key
    private Long id; //Primary key

    private String cookieId; //String gets stored in visitor's browser to mark their identity (replaces login)
    private double weight;
    private double height;
    private int age;
    private String sex;
    private String activity;
    private String goal;

    public Profile() {}

    //Setters and Getters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCookieId() {
        return cookieId;
    }

    public void setCookieId(String cookieId) {
        this.cookieId = cookieId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }
}
