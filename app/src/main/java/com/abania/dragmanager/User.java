package com.abania.dragmanager;

public class User {
    private String id;
    private String fullName;
    private String phone;
    private String city;
    private String address;
    private String avatarPath; // مسیر عکس پروفایل

    // سازنده
    public User(String id, String fullName, String phone, String city, String address, String avatarPath) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.city = city;
        this.address = address;
        this.avatarPath = avatarPath;
    }

    // Getters
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getAvatarPath() { return avatarPath; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCity(String city) { this.city = city; }
    public void setAddress(String address) { this.address = address; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
}