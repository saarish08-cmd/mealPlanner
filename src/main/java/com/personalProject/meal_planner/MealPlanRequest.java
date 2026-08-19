package com.personalProject.meal_planner;

public class MealPlanRequest {
    private double weight;
    private double height;
    private int age;
    private String sex;
    private String activity;
    private String goal;

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
}