package com.example.restaurantmanagementapplicationse;

public class ReservationData {
    private static ReservationData instance;
    private String tableNumber;
    private String reservationDate;
    private String reservationTime;
    private String reservationId;

    private ReservationData() {}

    public static ReservationData getInstance() {
        if (instance == null) {
            instance = new ReservationData();
        }
        return instance;
    }

    public void setReservationData(String tableNumber, String date, String time, String reservationId) {
        this.tableNumber = tableNumber;
        this.reservationDate = date;
        this.reservationTime = time;
        this.reservationId = reservationId;
    }

    public String getTableNumber() { return tableNumber; }
    public String getReservationDate() { return reservationDate; }
    public String getReservationTime() { return reservationTime; }
    public String getReservationId() { return reservationId; }

    public void clear() {
        tableNumber = null;
        reservationDate = null;
        reservationTime = null;
        reservationId = null;
    }
}