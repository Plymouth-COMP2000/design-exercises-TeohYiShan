package com.example.restaurantmanagementapplicationse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffReservationActivity extends AppCompatActivity {

    // UI Components
    private ImageButton logoutButton, notificationButton;
    private TextView reservationTitle;
    private LinearLayout menuTab, reservationsTab;

    // Reservation cards
    private CardView reservationCard1, reservationCard2, reservationCard3;
    private ImageButton cancelButton1, cancelButton2, cancelButton3;

    // TextViews for each card
    private TextView reservationMessage1, reservationMessage2, reservationMessage3;
    private TextView tableNoValue1, tableNoValue2, tableNoValue3;
    private TextView dateValue1, dateValue2, dateValue3;
    private TextView timeValue1, timeValue2, timeValue3;

    // Database
    private ReservationDatabaseHelper dbHelper;
    private NotificationDatabaseHelper notificationDbHelper;

    // Reservation data - using SimpleReservation
    private List<SimpleReservation> reservations;
    private static final String STAFF_USER_ID = "staff_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_reservation);

        initializeViews();
        initializeDatabase();
        loadReservationsFromDatabase();
        populateReservations();
        setupClickListeners();
        setupNavigation();
    }

    private void initializeViews() {
        // Header
        logoutButton = findViewById(R.id.logoutButton);
        notificationButton = findViewById(R.id.notificationButton);
        reservationTitle = findViewById(R.id.reservationTitle);

        // Reservation cards
        reservationCard1 = findViewById(R.id.reservationCard1);
        reservationCard2 = findViewById(R.id.reservationCard2);
        reservationCard3 = findViewById(R.id.reservationCard3);

        // Cancel buttons
        cancelButton1 = findViewById(R.id.cancelButton1);
        cancelButton2 = findViewById(R.id.cancelButton2);
        cancelButton3 = findViewById(R.id.cancelButton3);

        // TextViews for card 1
        reservationMessage1 = findViewById(R.id.reservationMessage1);
        tableNoValue1 = findViewById(R.id.tableNoValue1);
        dateValue1 = findViewById(R.id.dateValue1);
        timeValue1 = findViewById(R.id.timeValue1);

        // TextViews for card 2
        reservationMessage2 = findViewById(R.id.reservationMessage2);
        tableNoValue2 = findViewById(R.id.tableNoValue2);
        dateValue2 = findViewById(R.id.dateValue2);
        timeValue2 = findViewById(R.id.timeValue2);

        // TextViews for card 3
        reservationMessage3 = findViewById(R.id.reservationMessage3);
        tableNoValue3 = findViewById(R.id.tableNoValue3);
        dateValue3 = findViewById(R.id.dateValue3);
        timeValue3 = findViewById(R.id.timeValue3);

        // Navigation
        menuTab = findViewById(R.id.menuTab);
        reservationsTab = findViewById(R.id.reservationsTab);
    }

    private void initializeDatabase() {
        dbHelper = new ReservationDatabaseHelper(this);
        notificationDbHelper = new NotificationDatabaseHelper(this);
    }

    private void loadReservationsFromDatabase() {
        reservations = dbHelper.getAllReservations();

        if (reservations.isEmpty()) {
            Toast.makeText(this, "No reservations found", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateReservations() {
        if (reservations.isEmpty()) {
            reservationCard1.setVisibility(View.GONE);
            reservationCard2.setVisibility(View.GONE);
            reservationCard3.setVisibility(View.GONE);
            return;
        }

        // Show up to 3 reservations (most recent first)
        int maxToShow = Math.min(reservations.size(), 3);

        for (int i = 0; i < maxToShow; i++) {
            SimpleReservation reservation = reservations.get(i);
            updateReservationCard(i, reservation);
        }

        // Hide unused cards
        if (maxToShow < 3) reservationCard3.setVisibility(View.GONE);
        if (maxToShow < 2) reservationCard2.setVisibility(View.GONE);
        if (maxToShow < 1) reservationCard1.setVisibility(View.GONE);
    }

    private void updateReservationCard(int index, SimpleReservation reservation) {
        switch (index) {
            case 0:
                setCardData(reservation,
                        reservationMessage1,
                        tableNoValue1,
                        dateValue1,
                        timeValue1,
                        cancelButton1,
                        reservationCard1,
                        index);
                break;
            case 1:
                setCardData(reservation,
                        reservationMessage2,
                        tableNoValue2,
                        dateValue2,
                        timeValue2,
                        cancelButton2,
                        reservationCard2,
                        index);
                break;
            case 2:
                setCardData(reservation,
                        reservationMessage3,
                        tableNoValue3,
                        dateValue3,
                        timeValue3,
                        cancelButton3,
                        reservationCard3,
                        index);
                break;
        }
    }

    private void setCardData(SimpleReservation reservation,
                             TextView messageView,
                             TextView tableView,
                             TextView dateView,
                             TextView timeView,
                             ImageButton cancelButton,
                             CardView cardView,
                             int index) {

        // Set card data
        messageView.setText("New reservation for Table " + reservation.getTableNo());
        tableView.setText(reservation.getTableNo());
        dateView.setText(reservation.getDate());
        timeView.setText(reservation.getTime());

        // Set click listeners
        final int finalIndex = index;
        cancelButton.setOnClickListener(v -> showCancelConfirmation(finalIndex));
        cardView.setOnClickListener(v -> showReservationDetails(finalIndex));

        cardView.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        // Logout
        logoutButton.setOnClickListener(v -> logoutUser());

        // Notifications
        notificationButton.setOnClickListener(v -> openNotifications());
    }

    private void setupNavigation() {
        // Menu tab
        menuTab.setOnClickListener(v -> {
            navigateToMenu();
        });

        // Reservations tab (already active)
        reservationsTab.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Reservations", Toast.LENGTH_SHORT).show();
        });
    }

    private void showCancelConfirmation(int reservationIndex) {
        if (reservationIndex >= 0 && reservationIndex < reservations.size()) {
            SimpleReservation reservation = reservations.get(reservationIndex);

            new AlertDialog.Builder(this)
                    .setTitle("Cancel Reservation")
                    .setMessage("Cancel reservation for Table " + reservation.getTableNo() +
                            " on " + reservation.getDate() + " at " + reservation.getTime() + "?\n\n" +
                            "This will notify the guest.")
                    .setPositiveButton("Cancel Reservation", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelReservation(reservationIndex);
                        }
                    })
                    .setNegativeButton("Keep", null)
                    .show();
        }
    }

    private void cancelReservation(int reservationIndex) {
        if (reservationIndex >= 0 && reservationIndex < reservations.size()) {
            SimpleReservation reservation = reservations.get(reservationIndex);

            // DEBUG: Check what reservation we're trying to cancel
            Log.d("StaffReservation", "Attempting to cancel reservation: " +
                    reservation.getId() + " - " + reservation.getTableNo());

            // 1. Update reservation status in database
            boolean success = dbHelper.cancelReservation(reservation.getId());

            if (success) {
                Log.d("StaffReservation", "Database update successful");

                // 2. Get guest user ID for this reservation
                String guestUserId = dbHelper.getGuestUserId(reservation.getId());

                // 3. Create cancellation notification for guest
                createCancellationNotification(reservation, guestUserId);

                // 4. Create staff notification record
                createStaffCancellationNotification(reservation);

                // 5. Refresh the UI
                loadReservationsFromDatabase(); // Reload from DB
                populateReservations();

                Toast.makeText(this,
                        "Reservation cancelled for Table " + reservation.getTableNo(),
                        Toast.LENGTH_SHORT).show();

                // 6. Also update SharedPreferences notification
                updateSharedPreferencesNotification(reservation);

            } else {
                Log.e("StaffReservation", "Failed to cancel reservation in database");
                Toast.makeText(this, "Failed to cancel reservation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createCancellationNotification(SimpleReservation reservation, String guestUserId) {
        String notificationId = "NOTIF_CANCEL_" + System.currentTimeMillis();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        String title = "Reservation Cancelled";
        String message = "Your reservation for Table " + reservation.getTableNo() +
                " on " + reservation.getDate() + " at " + reservation.getTime() +
                " has been cancelled by staff.";

        Notification notification = new Notification(
                notificationId,
                title,
                message,
                reservation.getTableNo(),
                reservation.getDate(),
                reservation.getTime(),
                "cancelled_by_staff", // Use consistent type
                timestamp,
                reservation.getId()
        );

        // DEBUG: Log the notification creation
        Log.d("StaffReservation", "Creating cancellation notification for: " + guestUserId);
        Log.d("StaffReservation", "Notification type: cancelled_by_staff");

        // Save notification for the guest
        if (guestUserId != null && !guestUserId.isEmpty()) {
            long result = notificationDbHelper.addNotification(notification, guestUserId);
            if (result != -1) {
                Log.d("StaffReservation", "Guest notification saved successfully");
            } else {
                Log.e("StaffReservation", "Failed to save guest notification");
            }
        } else {
            // If no guest user ID, save to "guest_default"
            long result = notificationDbHelper.addNotification(notification, "guest_default");
            Log.d("StaffReservation", "Saved to guest_default, result: " + result);
        }

        // Also add to staff notifications for record keeping
        long staffResult = notificationDbHelper.addNotification(notification, STAFF_USER_ID);
        Log.d("StaffReservation", "Staff notification saved, result: " + staffResult);
    }

    private void createStaffCancellationNotification(SimpleReservation reservation) {
        String notificationId = "NOTIF_STAFF_CANCEL_" + System.currentTimeMillis();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        String title = "Reservation Cancelled";
        String message = "You cancelled reservation for Table " + reservation.getTableNo() +
                " on " + reservation.getDate() + " at " + reservation.getTime();

        Notification notification = new Notification(
                notificationId,
                title,
                message,
                reservation.getTableNo(),
                reservation.getDate(),
                reservation.getTime(),
                "cancelled",
                timestamp,
                reservation.getId()
        );

        notificationDbHelper.addNotification(notification, STAFF_USER_ID);
        Log.d("StaffReservation", "Staff notification created");
    }

    // Update SharedPreferences notification (optional)
    private void updateSharedPreferencesNotification(SimpleReservation reservation) {
        SharedPreferences prefs = getSharedPreferences("reservation_notifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Create a cancellation entry
        String key = "cancelled_" + System.currentTimeMillis();
        String value = reservation.getId() + "|" +
                reservation.getTableNo() + "|" +
                reservation.getDate() + "|" +
                reservation.getTime() + "|cancelled";

        editor.putString(key, value);
        editor.apply();

        Log.d("StaffReservation", "SharedPreferences updated for cancellation");
    }

    private void showReservationDetails(int reservationIndex) {
        if (reservationIndex >= 0 && reservationIndex < reservations.size()) {
            SimpleReservation reservation = reservations.get(reservationIndex);

            // Show details dialog - simplified since we only have basic info
            String details = "Reservation Details:\n\n" +
                    "ID: " + reservation.getId() + "\n" +
                    "Table: " + reservation.getTableNo() + "\n" +
                    "Date: " + reservation.getDate() + "\n" +
                    "Time: " + reservation.getTime() + "\n" +
                    "Status: " + reservation.getStatus() + "\n" +
                    "Created: " + reservation.getTimestamp();

            new AlertDialog.Builder(this)
                    .setTitle("Reservation Details")
                    .setMessage(details)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void navigateToMenu() {
        Intent intent = new Intent(this, StaffMenuActivity.class);
        startActivity(intent);
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void openNotifications() {
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("USER_TYPE", "staff");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to activity
        loadReservationsFromDatabase();
        populateReservations();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (notificationDbHelper != null) {
            notificationDbHelper.close();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("No", null)
                .show();
    }
}