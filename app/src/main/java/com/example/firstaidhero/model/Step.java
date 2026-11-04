package com.example.firstaidhero.model;

public class Step {
    private final int id;
    private final int caseId;
    private final int stepNumber;
    private final String instructionText;
    public Step(int id, int caseId, int stepNumber, String instructionText){
        this.id=id;
        this.caseId=caseId;
        this.stepNumber=stepNumber;
        this.instructionText=instructionText;
    }
    public int getId() {return id;}
    public int getCaseId() {return caseId;}
    public int getStepNumber() {return stepNumber;}
    public String getInstructionText() {return instructionText;}

}