package com.example.firstaidhero.model;

public class TreatedCase {
    private int id;
    private String description;
    private String date;
    private String photoPath;
    private String procedures;
    public TreatedCase(int id, String description, String date,
                       String photoPath, String procedures)
    {
        this.id=id;
        this.description=description;
        this.date=date;
        this.photoPath=photoPath;
        this.procedures=procedures;
    }
    public TreatedCase(String description, String date, String photoPath, String procedures){
        this(0,description,date,photoPath,procedures);
    }
    public int getId() {return id;}
    public String getDescription() {return description;}
    public String getDate() {return date;}
    public String getPhotoPath () {return photoPath;}
    public String getProcedures() {return procedures;}

    public void setDescription(String description) {this.description=description;}
    public void setDate (String date) {this.date=date;}
    public void setPhotoPath (String photoPath) {this.photoPath = photoPath;}
    public void setProcedures(String procedures) {this.procedures=procedures;}

}
