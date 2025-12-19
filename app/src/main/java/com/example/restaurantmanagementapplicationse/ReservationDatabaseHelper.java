package com.example.restaurantmanagementapplicationse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reservations.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_RESERVATIONS = "reservations";

    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TABLE_NO = "table_no";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_STATUS = "status"; // "successful" or "cancelled"
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_GUEST_USER_ID = "guest_user_id";

    // Create table SQL
    private static final String CREATE_TABLE_RESERVATIONS =
            "CREATE TABLE " + TABLE_RESERVATIONS + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_TABLE_NO + " TEXT NOT NULL," +
                    COLUMN_DATE + " TEXT NOT NULL," +
                    COLUMN_TIME + " TEXT NOT NULL," +
                    COLUMN_STATUS + " TEXT DEFAULT 'successful'," +
                    COLUMN_TIMESTAMP + " TEXT NOT NULL," +
                    COLUMN_GUEST_USER_ID + " TEXT" +
                    ")";

    public ReservationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RESERVATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        onCreate(db);
    }

    // Add a new reservation
    public long addReservation(String tableNo, String date, String time, String guestUserId, String reservationId) {
        SQLiteDatabase db = this.getWritableDatabase(); // This is correct here

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, reservationId);
        values.put(COLUMN_TABLE_NO, tableNo);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_STATUS, "successful");
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_GUEST_USER_ID, guestUserId);

        long result = db.insert(TABLE_RESERVATIONS, null, values);
        db.close();

        if (result != -1) {
            Log.d("ReservationDB", "Reservation saved: " + reservationId);
        } else {
            Log.e("ReservationDB", "Failed to save reservation: " + reservationId);
        }

        return result;
    }

    // Get all reservations
    public List<SimpleReservation> getAllReservations() {
        List<SimpleReservation> reservations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_ID, COLUMN_TABLE_NO, COLUMN_DATE,
                COLUMN_TIME, COLUMN_STATUS, COLUMN_TIMESTAMP,
                COLUMN_GUEST_USER_ID
        };

        String selection = COLUMN_STATUS + " = ?";
        String[] selectionArgs = {"successful"}; // Only show active reservations
        String orderBy = COLUMN_TIMESTAMP + " DESC";

        Cursor cursor = db.query(TABLE_RESERVATIONS, columns, selection,
                selectionArgs, null, null, orderBy, "10");

        if (cursor.moveToFirst()) {
            do {
                SimpleReservation reservation = new SimpleReservation(
                        cursor.getString(0), // id
                        cursor.getString(1), // tableNo
                        cursor.getString(2), // date
                        cursor.getString(3), // time
                        cursor.getString(4), // status
                        cursor.getString(5)  // timestamp
                );
                reservations.add(reservation);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return reservations;
    }

    // Delete reservation
    public boolean deleteReservation(String reservationId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {reservationId};

        int rowsAffected = db.delete(TABLE_RESERVATIONS, whereClause, whereArgs);

        db.close();

        if (rowsAffected > 0) {
            Log.d("ReservationDB", "Deleted reservation: " + reservationId);
            return true;
        } else {
            Log.e("ReservationDB", "No reservation to delete: " + reservationId);
            return false;
        }
    }

    // Cancel reservation (update status)
    public boolean cancelReservation(String reservationId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, "cancelled");

        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {reservationId};

        int rowsAffected = db.update(TABLE_RESERVATIONS, values, whereClause, whereArgs);

        db.close();

        if (rowsAffected > 0) {
            Log.d("ReservationDB", "Cancelled reservation: " + reservationId);
            return true;
        } else {
            Log.e("ReservationDB", "Failed to cancel reservation: " + reservationId);
            return false;
        }
    }

    // Get reservation by ID
    public SimpleReservation getReservationById(String reservationId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_ID, COLUMN_TABLE_NO, COLUMN_DATE,
                COLUMN_TIME, COLUMN_STATUS, COLUMN_TIMESTAMP
        };

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {reservationId};

        Cursor cursor = db.query(TABLE_RESERVATIONS, columns, selection,
                selectionArgs, null, null, null);

        SimpleReservation reservation = null;
        if (cursor.moveToFirst()) {
            reservation = new SimpleReservation(
                    cursor.getString(0), // id
                    cursor.getString(1), // tableNo
                    cursor.getString(2), // date
                    cursor.getString(3), // time
                    cursor.getString(4), // status
                    cursor.getString(5)  // timestamp
            );
            Log.d("ReservationDB", "Found reservation: " + reservation.getId());
        } else {
            Log.d("ReservationDB", "No reservation found with ID: " + reservationId);
        }

        cursor.close();
        db.close();
        return reservation;
    }

    // Get guest user ID for a reservation
    public String getGuestUserId(String reservationId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_GUEST_USER_ID};
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {reservationId};

        Cursor cursor = db.query(TABLE_RESERVATIONS, columns, selection,
                selectionArgs, null, null, null);

        String guestUserId = null;
        if (cursor.moveToFirst()) {
            guestUserId = cursor.getString(0);
        }

        cursor.close();
        db.close();
        return guestUserId;
    }
}