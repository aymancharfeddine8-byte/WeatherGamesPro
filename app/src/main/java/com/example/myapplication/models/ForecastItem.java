package com.example.myapplication.models;

public class ForecastItem {
    public String day;
    public double temp;
    public String icon;

    public ForecastItem(String day, double temp, String icon) {
        this.day = day;
        this.temp = temp;
        this.icon = icon;
    }
}
