package com.example.firstaidhero.model;

public class Contact {
    private int id;
    private String name;
    private String phoneNumber;

    public Contact(int id,String name, String phoneNumber) {
        this.id=id;
        this.name=name;
        this.phoneNumber=phoneNumber;
    }
 public Contact(String name, String phoneNumber){
     this(0,name,phoneNumber);}

    public int getId(){return id;}
    public String getName() {return name;}
    public String getPhoneNumber() {return phoneNumber;}
    public void setName(String name) {this.name=name;}
    public void setPhoneNumber(String phoneNumber) {this.phoneNumber=phoneNumber;}
}
