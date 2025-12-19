package com.example.restaurantmanagementapplicationse;

public class SimpleReservation {
    private String id;
    private String tableNo;
    private String date;
    private String time;
    private String status; // "successful" or "cancelled"
    private String timestamp;

    public SimpleReservation(String id, String tableNo, String date, String time, String status) {
        this.id = id;
        this.tableNo = tableNo;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public SimpleReservation(String id, String tableNo, String date, String time, String status, String timestamp) {
        this.id = id;
        this.tableNo = tableNo;
        this.date = date;
        this.time = time;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters
    public String getId() { return id; }
    public String getTableNo() { return tableNo; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
}