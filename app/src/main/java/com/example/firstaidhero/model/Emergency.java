package com.example.firstaidhero.model;

public class Emergency {
    private final int id;
    private final String title;
    private final String dangerLevel;
    private final String symptoms;
    public Emergency(int id, String title, String dangerLevel, String symptoms) {
        this.id=id;
        this.title=title;
        this.dangerLevel=dangerLevel;
        this.symptoms=symptoms;
    }
public int getId() {return id;}
public String getTitle() {return title;}
public String getDangerLevel() {return dangerLevel;}
public String getSymptoms() {return symptoms;}
}
