package com.abania.dragmanager;

public class Medicine {
    private int id;
    private String name;
    private String englishName;
    private String dosage;
    private String expiryDate;
    private String type;
    private int quantity;
    private String disease;
    private String routine;
    private String instructions;

    public Medicine(String name, String dosage, String expiryDate, String type,
                    int quantity, String disease, String routine, String instructions) {
        this.name = name;
        this.dosage = dosage;
        this.expiryDate = expiryDate;
        this.type = type;
        this.quantity = quantity;
        this.disease = disease;
        this.routine = routine;
        this.instructions = instructions;
    }

    public Medicine(int id, String name, String dosage, String expiryDate, String type,
                    int quantity, String disease, String routine, String instructions) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.expiryDate = expiryDate;
        this.type = type;
        this.quantity = quantity;
        this.disease = disease;
        this.routine = routine;
        this.instructions = instructions;
    }

    // ---------- GETTERS ----------
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEnglishName() { return englishName; }
    public String getDosage() { return dosage; }
    public String getExpiryDate() { return expiryDate; }
    public String getType() { return type; }
    public int getQuantity() { return quantity; }
    public String getDisease() { return disease; }
    public String getRoutine() { return routine; }
    public String getInstructions() { return instructions; }

    // ---------- SETTERS ----------
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setType(String type) { this.type = type; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setDisease(String disease) { this.disease = disease; }
    public void setRoutine(String routine) { this.routine = routine; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}