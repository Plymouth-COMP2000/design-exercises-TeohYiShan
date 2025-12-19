package com.example.restaurantmanagementapplicationse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class NotificationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notifications.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_NOTIFICATIONS = "notifications";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_TABLE_NO = "table_no";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_RESERVATION_ID = "reservation_id";
    private static final String COLUMN_IS_READ = "is_read";
    private static final String COLUMN_USER_ID = "user_id";

    // Create table SQL
    private static final String CREATE_TABLE_NOTIFICATIONS =
            "CREATE TABLE " + TABLE_NOTIFICATIONS + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_MESSAGE + " TEXT," +
                    COLUMN_TABLE_NO + " TEXT," +
                    COLUMN_DATE + " TEXT," +
                    COLUMN_TIME + " TEXT," +
                    COLUMN_TYPE + " TEXT," +
                    COLUMN_TIMESTAMP + " TEXT," +
                    COLUMN_RESERVATION_ID + " TEXT," +
                    COLUMN_IS_READ + " INTEGER DEFAULT 0," +
                    COLUMN_USER_ID + " TEXT" +
                    ")";

    public NotificationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(db);
    }

    // Add a new notification
    public long addNotification(Notification notification, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, notification.getId());
        values.put(COLUMN_TITLE, notification.getTitle());
        values.put(COLUMN_MESSAGE, notification.getMessage());
        values.put(COLUMN_TABLE_NO, notification.getTableNo());
        values.put(COLUMN_DATE, notification.getDate());
        values.put(COLUMN_TIME, notification.getTime());
        values.put(COLUMN_TYPE, notification.getType());
        values.put(COLUMN_TIMESTAMP, notification.getTimestamp());
        values.put(COLUMN_RESERVATION_ID, notification.getReservationId());
        values.put(COLUMN_IS_READ, 0); // 0 = unread, 1 = read
        values.put(COLUMN_USER_ID, userId);

        long result = db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
        return result;
    }

    // Get all notifications for a user
    public List<Notification> getAllNotifications(String userId) {
        List<Notification> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_ID, COLUMN_TITLE, COLUMN_MESSAGE, COLUMN_TABLE_NO,
                COLUMN_DATE, COLUMN_TIME, COLUMN_TYPE, COLUMN_TIMESTAMP,
                COLUMN_RESERVATION_ID
        };

        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId};
        String orderBy = COLUMN_TIMESTAMP + " DESC";

        Cursor cursor = db.query(TABLE_NOTIFICATIONS, columns, selection,
                selectionArgs, null, null, orderBy, "10"); // Limit to 10 most recent

        if (cursor.moveToFirst()) {
            do {
                Notification notification = new Notification(
                        cursor.getString(0), // id
                        cursor.getString(1), // title
                        cursor.getString(2), // message
                        cursor.getString(3), // tableNo
                        cursor.getString(4), // date
                        cursor.getString(5), // time
                        cursor.getString(6), // type
                        cursor.getString(7), // timestamp
                        cursor.getString(8)  // reservationId
                );
                notifications.add(notification);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notifications;
    }

    // Mark a notification as read
    public void markAsRead(String notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_READ, 1);

        db.update(TABLE_NOTIFICATIONS, values,
                COLUMN_ID + " = ?", new String[]{notificationId});
        db.close();
    }

    // Mark all notifications as read
    public void markAllAsRead(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_READ, 1);

        db.update(TABLE_NOTIFICATIONS, values,
                COLUMN_USER_ID + " = ? AND " + COLUMN_IS_READ + " = 0",
                new String[]{userId});
        db.close();
    }

    // Get unread notification count
    public int getUnreadCount(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS +
                " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_IS_READ + " = 0";
        Cursor cursor = db.rawQuery(query, new String[]{userId});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    // Delete old notifications (keep only last 50)
    public void cleanupOldNotifications(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM " + TABLE_NOTIFICATIONS +
                " WHERE " + COLUMN_ID + " IN (" +
                "SELECT " + COLUMN_ID + " FROM " + TABLE_NOTIFICATIONS +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC" +
                " LIMIT -1 OFFSET 50)";

        db.execSQL(query, new String[]{userId});
        db.close();
    }
}