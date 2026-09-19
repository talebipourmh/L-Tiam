package com.abania.dragmanager;

public class Donation {
    private int id;
    private String medicine_name;
    private String dosage;
    private int quantity;
    private String expiry_date;
    private String donor_name;
    private String city;
    private String phone;
    private String status; // "pending", "approved", "rejected", "completed"
    private String persianDate; // تاریخ شمسی

    // سازنده بدون وضعیت و تاریخ شمسی
    public Donation(int id, String medicine_name, String dosage, int quantity,
                    String expiry_date, String donor_name, String city, String phone) {
        this.id = id;
        this.medicine_name = medicine_name;
        this.dosage = dosage;
        this.quantity = quantity;
        this.expiry_date = expiry_date;
        this.donor_name = donor_name;
        this.city = city;
        this.phone = phone;
        this.status = "pending";
        this.persianDate = getCurrentPersianDate();
    }

    // سازنده کامل با وضعیت و تاریخ شمسی
    public Donation(int id, String medicine_name, String dosage, int quantity,
                    String expiry_date, String donor_name, String city, String phone,
                    String status, String persianDate) {
        this.id = id;
        this.medicine_name = medicine_name;
        this.dosage = dosage;
        this.quantity = quantity;
        this.expiry_date = expiry_date;
        this.donor_name = donor_name;
        this.city = city;
        this.phone = phone;
        this.status = status;
        this.persianDate = persianDate;
    }

    // دریافت تاریخ شمسی فعلی
    private String getCurrentPersianDate() {
        // استفاده از کلاس PersianDateHelper
        return PersianDateHelper.getCurrentDate();
    }

    // ---------- GETTERS ----------
    public int getId() { return id; }
    public String getMedicine_name() { return medicine_name; }
    public String getDosage() { return dosage; }
    public int getQuantity() { return quantity; }
    public String getExpiry_date() { return expiry_date; }
    public String getDonor_name() { return donor_name; }
    public String getCity() { return city; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public String getPersianDate() { return persianDate; }

    // ---------- SETTERS ----------
    public void setId(int id) { this.id = id; }
    public void setMedicine_name(String medicine_name) { this.medicine_name = medicine_name; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setExpiry_date(String expiry_date) { this.expiry_date = expiry_date; }
    public void setDonor_name(String donor_name) { this.donor_name = donor_name; }
    public void setCity(String city) { this.city = city; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setStatus(String status) { this.status = status; }
    public void setPersianDate(String persianDate) { this.persianDate = persianDate; }
}