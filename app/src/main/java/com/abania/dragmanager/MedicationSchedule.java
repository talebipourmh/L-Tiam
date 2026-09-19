package com.abania.dragmanager;

public class MedicationSchedule {
    private int id;
    private String medicineName;
    private String dosage;
    private int totalQuantity;
    private int remainingQuantity;
    private int intervalHours;    // فاصله زمانی بین هر نوبت (ساعت)
    private String startTime;     // ساعت شروع (مثلاً "08:00")
    private String endDate;       // تاریخ پایان دوره (محاسبه خودکار)
    private boolean isActive;     // فعال/غیرفعال
    private String scheduleType;  // "daily", "hourly", "custom"
    private int retryMinutes; // 0 = غیرفعال

    // Getter و Setter اضافه کنید
    public int getRetryMinutes() { return retryMinutes; }
    public void setRetryMinutes(int retryMinutes) { this.retryMinutes = retryMinutes; }
		
    // سازنده برای ایجاد جدید
    public MedicationSchedule(String medicineName, String dosage, int totalQuantity,
                              int intervalHours, String startTime, String scheduleType) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
        this.intervalHours = intervalHours;
        this.startTime = startTime;
        this.scheduleType = scheduleType;
        this.isActive = true;
    }

    // سازنده برای خواندن از دیتابیس
    public MedicationSchedule(int id, String medicineName, String dosage, int totalQuantity,
                              int remainingQuantity, int intervalHours, String startTime,
                              String endDate, boolean isActive, String scheduleType) {
        this.id = id;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = remainingQuantity;
        this.intervalHours = intervalHours;
        this.startTime = startTime;
        this.endDate = endDate;
        this.isActive = isActive;
        this.scheduleType = scheduleType;
    }

    // ---------- GETTERS ----------
    public int getId() { return id; }
    public String getMedicineName() { return medicineName; }
    public String getDosage() { return dosage; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public int getIntervalHours() { return intervalHours; }
    public String getStartTime() { return startTime; }
    public String getEndDate() { return endDate; }
    public boolean isActive() { return isActive; }
    public String getScheduleType() { return scheduleType; }

    // ---------- SETTERS ----------
    public void setId(int id) { this.id = id; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public void setIntervalHours(int intervalHours) { this.intervalHours = intervalHours; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setActive(boolean active) { isActive = active; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }
}