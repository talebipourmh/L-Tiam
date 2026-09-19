package com.abania.dragmanager;

import android.content.Context;

public class DonationRequest {
    private int id;
    private String medicineName;
    private int medicineId;
    private int quantity;
    private String donorName;
    private String donorPhone;
    private String status; // PENDING, ACCEPTED, COMPLETED, REJECTED
    private long timestamp;
    private String message;
    
    // فیلدهای اضافی برای RequestActivity
    private String dosage;
    private String city;
    private String requesterPhone;
    private String requestDate;

    // فیلد جدید: نوع رکورد در جدول مشترک donation_requests
    private String requestType; // "DONATION" یا "REQUEST"

    // =============== Constructors ===============
    
    public DonationRequest() {}

    public DonationRequest(String medicineName, int medicineId, int quantity, 
                           String donorName, String donorPhone, String message) {
        this.medicineName = medicineName;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.donorName = donorName;
        this.donorPhone = donorPhone;
        this.status = "PENDING";
        this.timestamp = System.currentTimeMillis();
        this.message = message;
        this.requestType = "DONATION";
    }

    public DonationRequest(String medicineName, String dosage, String city, 
                           int quantity, String requesterPhone, 
                           String requestDate, String status, String userId) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.city = city;
        this.quantity = quantity;
        this.requesterPhone = requesterPhone;
        this.requestDate = requestDate;
        this.status = status;
        this.donorName = userId;
        this.donorPhone = requesterPhone;
        this.timestamp = System.currentTimeMillis();
        this.requestType = "REQUEST";
    }

    // سازنده جدید: وقتی نوع را صریحاً از AddMedicineActivity پاس می‌دهیم
    public DonationRequest(String medicineName, String dosage, String city, 
                           int quantity, String requesterPhone, 
                           String requestDate, String status, String userId,
                           String requestType) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.city = city;
        this.quantity = quantity;
        this.requesterPhone = requesterPhone;
        this.requestDate = requestDate;
        this.status = status;
        this.donorName = userId;
        this.donorPhone = requesterPhone;
        this.timestamp = System.currentTimeMillis();
        this.requestType = requestType;
    }

    // =============== Getters ===============
    public int getId() { return id; }
    public String getMedicineName() { return medicineName; }
    public int getMedicineId() { return medicineId; }
    public int getQuantity() { return quantity; }
    public String getDonorName() { return donorName; }
    public String getDonorPhone() { return donorPhone; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    
    public String getDosage() { return dosage; }
    public String getCity() { return city; }
    public String getRequesterPhone() { return requesterPhone; }
    public String getRequestDate() { return requestDate; }

    public String getRequestType() { return requestType; }

    // =============== Setters ===============
    public void setId(int id) { this.id = id; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setDonorName(String donorName) { this.donorName = donorName; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setMessage(String message) { this.message = message; }
    
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setCity(String city) { this.city = city; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public void setRequestType(String requestType) { this.requestType = requestType; }

    // =============== متدهای کمکی ===============
    
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isAccepted() {
        return "ACCEPTED".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isRejected() {
        return "REJECTED".equals(status);
    }

    public boolean isDonation() {
        return "DONATION".equals(requestType);
    }

    public boolean isRequestType() {
        return "REQUEST".equals(requestType);
    }

    public String getStatusPersian() {
        switch (status) {
            case "PENDING": return "در انتظار";
            case "ACCEPTED": return "قبول شده";
            case "COMPLETED": return "تکمیل شده";
            case "REJECTED": return "رد شده";
            default: return status;
        }
    }

    public int getStatusColor(Context context) {
        switch (status) {
            case "PENDING": return context.getColor(android.R.color.holo_orange_dark);
            case "ACCEPTED": return context.getColor(android.R.color.holo_green_dark);
            case "COMPLETED": return context.getColor(android.R.color.holo_blue_dark);
            case "REJECTED": return context.getColor(android.R.color.holo_red_dark);
            default: return context.getColor(android.R.color.black);
        }
    }
}