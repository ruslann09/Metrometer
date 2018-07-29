package com.proger.ruslan.advancedact;

import java.io.Serializable;

public class DataMatches implements Serializable {
    private long id;
    private double area, perimeter;
    private int dots;
    private String dotsRelatives;
    private String date;
    private String type;
    private String note;
    private String place;
    private double percent_of_quality;

    public DataMatches () {

    }

    public DataMatches (long id, double area, double perimeter, int dots, String dotsRelatives, String date,
                        String type, String note, String place, double percent_of_quality) {
        this.id = id;
        this.area = area;
        this.perimeter = perimeter;
        this.dots = dots;
        this.dotsRelatives = dotsRelatives;
        this.date = date;
        this.type = type;
        this.note = note;
        this.place = place;
        this.percent_of_quality = percent_of_quality;
    }
    public long getId() {
        return id;
    }
    public double getArea() {
        return area;
    }
    public double getPerimeter() {
        return perimeter;
    }
    public int getDots() {
        return dots;
    }
    public String getDotsRelatives () {return dotsRelatives;}
    public String getDate () {return date;}
    public String getType () {return type;}
    public String getNote () {return note;}
    public String getPlace () {return place;}
    public double getPercent_of_quality () {return percent_of_quality;}
}
