package com.abania.dragmanager;

public class DonationItem {
    private int id;
    private String medicineName;
    private String medicineDosage;
    private String medicineType;
    private int quantity;
    private String donorName;
    private String donorPhone;
    private String donorCity;      // ✅ اضافه شد
    private String donorProvince;  // ✅ اضافه شد
    private String donationDate;
    private String status;

    // =============== Constructors ===============
    public DonationItem() {}

    public DonationItem(String medicineName, String medicineDosage, String medicineType, 
                        int quantity, String donorName, String donorPhone, 
                        String donationDate, String status) {
        this.medicineName = medicineName;
        this.medicineDosage = medicineDosage;
        this.medicineType = medicineType;
        this.quantity = quantity;
        this.donorName = donorName;
        this.donorPhone = donorPhone;
        this.donationDate = donationDate;
        this.status = status;
    }

    public DonationItem(String medicineName, String medicineDosage, int quantity, 
                        String donorName, String donorPhone) {
        this.medicineName = medicineName;
        this.medicineDosage = medicineDosage;
        this.quantity = quantity;
        this.donorName = donorName;
        this.donorPhone = donorPhone;
        this.status = "PENDING";
        this.donationDate = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", 
                java.util.Locale.getDefault()).format(new java.util.Date());
    }

    // =============== Getters ===============
    public int getId() { return id; }
    public String getMedicineName() { return medicineName; }
    public String getMedicineDosage() { return medicineDosage; }
    public String getMedicineType() { return medicineType; }
    public int getQuantity() { return quantity; }
    public String getDonorName() { return donorName; }
    public String getDonorPhone() { return donorPhone; }
    public String getDonorCity() { return donorCity; }        // ✅ جدید
    public String getDonorProvince() { return donorProvince; } // ✅ جدید
    public String getDonationDate() { return donationDate; }
    public String getStatus() { return status; }

    // =============== Setters ===============
    public void setId(int id) { this.id = id; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public void setMedicineDosage(String medicineDosage) { this.medicineDosage = medicineDosage; }
    public void setMedicineType(String medicineType) { this.medicineType = medicineType; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setDonorName(String donorName) { this.donorName = donorName; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }
    public void setDonorCity(String donorCity) { this.donorCity = donorCity; }        // ✅ جدید
    public void setDonorProvince(String donorProvince) { this.donorProvince = donorProvince; } // ✅ جدید
    public void setDonationDate(String donationDate) { this.donationDate = donationDate; }
    public void setStatus(String status) { this.status = status; }

    // =============== متدهای کمکی ===============
    @Override
    public String toString() {
        return "DonationItem{" +
                "id=" + id +
                ", medicineName='" + medicineName + '\'' +
                ", quantity=" + quantity +
                ", donorName='" + donorName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public boolean isCompleted() { return "COMPLETED".equals(status); }
    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isAccepted() { return "ACCEPTED".equals(status); }
    public boolean isRejected() { return "REJECTED".equals(status); }

    public String getStatusPersian() {
        switch (status) {
            case "PENDING": return "در انتظار";
            case "ACCEPTED": return "پذیرفته شده";
            case "REJECTED": return "رد شده";
            case "COMPLETED": return "تکمیل شده";
            default: return status;
        }
    }

    public int getStatusColor(android.content.Context context) {
        switch (status) {
            case "PENDING": return context.getColor(android.R.color.holo_orange_dark);
            case "ACCEPTED": return context.getColor(android.R.color.holo_green_dark);
            case "REJECTED": return context.getColor(android.R.color.holo_red_dark);
            case "COMPLETED": return context.getColor(android.R.color.holo_blue_dark);
            default: return context.getColor(android.R.color.black);
        }
    }
}