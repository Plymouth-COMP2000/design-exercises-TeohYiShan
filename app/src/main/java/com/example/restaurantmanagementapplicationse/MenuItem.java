package com.example.restaurantmanagementapplicationse;

import android.graphics.Bitmap;

public class MenuItem {
    private int id;
    private String name;
    private double price;
    private String description;
    private String imageBase64; // Store image as Base64 string
    private String category;
    private String createdAt;
    private String updatedAt;

    // For temporary bitmap storage (not saved to database)
    private transient Bitmap imageBitmap;

    // Constructors
    public MenuItem() {}

    public MenuItem(String name, double price, String description, String category) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Bitmap getImageBitmap() { return imageBitmap; }
    public void setImageBitmap(Bitmap imageBitmap) { this.imageBitmap = imageBitmap; }

    // Helper methods
    public String getFormattedPrice() {
        return String.format("RM%.2f", price);
    }

    @Override
    public String toString() {
        return name + " - " + getFormattedPrice();
    }
}