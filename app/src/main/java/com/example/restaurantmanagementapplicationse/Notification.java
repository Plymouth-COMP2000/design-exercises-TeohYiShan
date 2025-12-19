package com.example.restaurantmanagementapplicationse;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String tableNo;
    private String date;
    private String time;
    private String type;
    private String timestamp;
    private String reservationId;

    public Notification(String id, String title, String message, String tableNo,
                        String date, String time, String type, String timestamp, String reservationId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.tableNo = tableNo;
        this.date = date;
        this.time = time;
        this.type = type;
        this.timestamp = timestamp;
        this.reservationId = reservationId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTableNo() { return tableNo; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getType() { return type; }
    public String getTimestamp() { return timestamp; }
    public String getReservationId() { return reservationId; }
}