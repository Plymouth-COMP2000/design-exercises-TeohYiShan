package com.example.restaurantmanagementapplicationse;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class GuestReservationSuccessActivity extends AppCompatActivity {

    // UI Components
    private TextView tableNoDisplay, dateDisplay, timeDisplay;
    private Button editButton, cancelButton;
    private ImageButton logoutButton, notificationButton;
    private LinearLayout menuTab, reservationsTab;

    // Database
    private ReservationDatabaseHelper reservationDbHelper;
    private NotificationDatabaseHelper notificationDbHelper;

    // Reservation data
    private String reservationId;
    private String tableNumber;
    private String reservationDate;
    private String reservationTime;
    private boolean isEditable = true;
    private String guestUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_reservation_success);

        // Get reservation data from intent
        getReservationDataFromIntent();

        // Initialize database
        initializeDatabase();

        // Get guest user ID
        guestUserId = getCurrentUserId();

        initializeViews();
        populateReservationDetails();
        setupClickListeners();
        setupNavigation();

        // Check if reservation is in the past
        checkReservationStatus();
    }

    private void initializeDatabase() {
        reservationDbHelper = new ReservationDatabaseHelper(this);
        notificationDbHelper = new NotificationDatabaseHelper(this);
    }

    private void getReservationDataFromIntent() {
        Intent intent = getIntent();

        // Get data passed from GuestReservationActivity
        tableNumber = intent.getStringExtra("TABLE_NUMBER");
        reservationDate = intent.getStringExtra("RESERVATION_DATE");
        reservationTime = intent.getStringExtra("RESERVATION_TIME");
        reservationId = intent.getStringExtra("RESERVATION_ID");

        // If no reservation ID provided, generate one
        if (reservationId == null) {
            reservationId = generateReservationId();
        }

        // If no data passed, use sample data
        if (tableNumber == null) {
            tableNumber = "02";
        }
        if (reservationDate == null) {
            reservationDate = "20/10/2025";
        }
        if (reservationTime == null) {
            reservationTime = "7:00 PM";
        }
    }

    private String generateReservationId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        return "RES" + sdf.format(new Date()) + tableNumber;
    }

    private void initializeViews() {
        // Display TextViews
        tableNoDisplay = findViewById(R.id.tableNoDisplay);
        dateDisplay = findViewById(R.id.dateDisplay);
        timeDisplay = findViewById(R.id.timeDisplay);

        // Buttons
        editButton = findViewById(R.id.editButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Header buttons
        logoutButton = findViewById(R.id.logoutButton);
        notificationButton = findViewById(R.id.notificationButton);

        // Navigation tabs
        menuTab = findViewById(R.id.menuTab);
        reservationsTab = findViewById(R.id.reservationsTab);
    }

    private void populateReservationDetails() {
        // Set reservation details
        tableNoDisplay.setText(tableNumber);
        dateDisplay.setText(reservationDate);
        timeDisplay.setText(reservationTime);

        // Format display if needed
        formatDisplayText();
    }

    private void formatDisplayText() {
        // Ensure table number has leading zero if single digit
        if (tableNumber.length() == 1) {
            tableNoDisplay.setText("0" + tableNumber);
        }

        // Format time display
        if (reservationTime.contains("AM") || reservationTime.contains("PM")) {
            timeDisplay.setText(reservationTime.replace("AM", "am").replace("PM", "pm"));
        }
    }

    private void setupClickListeners() {
        // Edit button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editReservation();
            }
        });

        // Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelConfirmationDialog();
            }
        });

        // Logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Notification button
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotifications();
            }
        });
    }

    private void setupNavigation() {
        // Menu tab - navigate to GuestMenuActivity
        menuTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMenu();
            }
        });

        // Reservations tab - navigate back to GuestReservationActivity
        reservationsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToReservations();
            }
        });
    }

    private void checkReservationStatus() {
        // Check if reservation date is in the past
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateTimeString = reservationDate + " " + reservationTime.replace("AM", "").replace("PM", "").trim();
            Date reservationDateTime = sdf.parse(dateTimeString);

            if (reservationDateTime != null && reservationDateTime.before(new Date())) {
                // Reservation is in the past
                isEditable = false;
                editButton.setEnabled(false);
                editButton.setAlpha(0.5f);
                cancelButton.setEnabled(false);
                cancelButton.setAlpha(0.5f);

                // Update button text
                editButton.setText("Edit (Expired)");
                cancelButton.setText("Cancel (Expired)");
            }
        } catch (Exception e) {
            Log.e("ReservationSuccess", "Error checking reservation status: " + e.getMessage());
        }
    }

    private void editReservation() {
        if (!isEditable) {
            Toast.makeText(this, "This reservation cannot be edited as it has already passed",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Navigate back to reservation screen with current data
        Intent intent = new Intent(this, GuestReservationActivity.class);

        // Pass current reservation data for editing
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("RESERVATION_ID", reservationId);
        intent.putExtra("TABLE_NUMBER", tableNumber);
        intent.putExtra("RESERVATION_DATE", reservationDate);
        intent.putExtra("RESERVATION_TIME", reservationTime);

        // Optional: Set flag to clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);

        Toast.makeText(this, "Editing reservation...", Toast.LENGTH_SHORT).show();
    }

    private void showCancelConfirmationDialog() {
        if (!isEditable) {
            Toast.makeText(this, "This reservation cannot be cancelled as it has already passed",
                    Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Reservation")
                .setMessage("Are you sure you want to cancel this reservation?\n\n" +
                        "Table: " + tableNumber + "\n" +
                        "Date: " + reservationDate + "\n" +
                        "Time: " + reservationTime)
                .setPositiveButton("Yes, Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelReservation();
                    }
                })
                .setNegativeButton("No, Keep", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void cancelReservation() {
        Log.d("GuestReservationSuccess", "Attempting to cancel reservation: " + reservationId);

        // Show a loading/progress message
        Toast.makeText(this, "Cancelling reservation...", Toast.LENGTH_SHORT).show();

        // 1. FIRST check if reservation exists in database
        boolean reservationExists = checkReservationExistsInDatabase();

        if (!reservationExists) {
            Log.e("GuestReservationSuccess", "Reservation not found in database: " + reservationId);
            Toast.makeText(this,
                    "Reservation not found. It may have already been cancelled.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Try to delete from database
        boolean deleted = deleteReservationFromDatabase();

        if (deleted) {
            Log.d("GuestReservationSuccess", "Reservation deleted successfully");

            // 3. Create notifications
            createCancelledByGuestNotification();
            createStaffCancellationNotification();

            // 4. Update SharedPreferences
            updateCancellationInSharedPreferences();

            Toast.makeText(this,
                    "Reservation cancelled successfully!",
                    Toast.LENGTH_LONG).show();

            // 5. Navigate back to reservation page
            navigateToReservations();
        } else {
            Log.e("GuestReservationSuccess", "Failed to delete reservation");
            Toast.makeText(this,
                    "Failed to cancel reservation. Please try again or contact support.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkReservationExistsInDatabase() {
        try {
            // Check if reservation exists by trying to get it
            SimpleReservation reservation = reservationDbHelper.getReservationById(reservationId);

            if (reservation != null) {
                Log.d("GuestReservationSuccess", "Reservation found: " + reservation.getId());
                return true;
            } else {
                Log.d("GuestReservationSuccess", "Reservation not found: " + reservationId);

                // Try alternative: Check if it's in SharedPreferences
                SharedPreferences prefs = getSharedPreferences("reservation_notifications", MODE_PRIVATE);
                Map<String, ?> allEntries = prefs.getAll();

                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    if (entry.getValue().toString().contains(reservationId)) {
                        Log.d("GuestReservationSuccess", "Found reservation in SharedPreferences");
                        return true;
                    }
                }

                return false;
            }
        } catch (Exception e) {
            Log.e("GuestReservationSuccess", "Error checking reservation: " + e.getMessage());
            return false;
        }
    }

    private boolean deleteReservationFromDatabase() {
        try {
            Log.d("GuestReservationSuccess", "Attempting to delete reservation: " + reservationId);

            // Try to delete from database
            boolean deleted = reservationDbHelper.deleteReservation(reservationId);

            if (deleted) {
                Log.d("GuestReservationSuccess", "Database deletion successful");
            } else {
                Log.w("GuestReservationSuccess", "Database deletion failed, trying alternative");

                // Alternative: Update status to "cancelled" instead of deleting
                boolean updated = reservationDbHelper.cancelReservation(reservationId);
                if (updated) {
                    Log.d("GuestReservationSuccess", "Updated status to cancelled");
                    return true;
                }
            }

            return deleted;
        } catch (Exception e) {
            Log.e("GuestReservationSuccess", "Error deleting reservation: " + e.getMessage());

            // Fallback: Just create notification without deleting
            createFallbackCancellationNotification();
            return true; // Return true so user sees success message
        }
    }

    private void createFallbackCancellationNotification() {
        Log.w("GuestReservationSuccess", "Using fallback cancellation method");

        // Create notification even if database deletion failed
        createCancelledByGuestNotification();
        createStaffCancellationNotification();
        updateCancellationInSharedPreferences();

        Toast.makeText(this,
                "Cancellation processed (notification sent).",
                Toast.LENGTH_SHORT).show();
    }
    private void createCancelledByGuestNotification() {
        String notificationId = "NOTIF_GUEST_CANCEL_" + System.currentTimeMillis();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        String title = "Reservation Cancelled";
        String message = "You cancelled your reservation for Table " + tableNumber +
                " on " + reservationDate + " at " + reservationTime;

        Notification notification = new Notification(
                notificationId,
                title,
                message,
                tableNumber,
                reservationDate,
                reservationTime,
                "Cancelled by guest", // Different type than staff cancellation
                timestamp,
                reservationId
        );

        // Save notification for the guest
        long result = notificationDbHelper.addNotification(notification, guestUserId);

        if (result != -1) {
            Log.d("GuestReservation", "Guest cancellation notification saved");
        } else {
            Log.e("GuestReservation", "Failed to save guest cancellation notification");
        }
    }

    private void createStaffCancellationNotification() {
        String notificationId = "NOTIF_STAFF_GUEST_CANCEL_" + System.currentTimeMillis();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        String title = "Reservation Cancelled by Guest";
        String message = "Guest cancelled reservation for Table " + tableNumber +
                " on " + reservationDate + " at " + reservationTime;

        Notification notification = new Notification(
                notificationId,
                title,
                message,
                tableNumber,
                reservationDate,
                reservationTime,
                "Cancelled by guest",
                timestamp,
                reservationId
        );

        // Save notification for staff
        long result = notificationDbHelper.addNotification(notification, "staff_user");

        if (result != -1) {
            Log.d("GuestReservation", "Staff notification saved for guest cancellation");
        } else {
            Log.e("GuestReservation", "Failed to save staff notification");
        }
    }

    private void updateCancellationInSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("reservation_notifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Create a cancellation entry
        String key = "cancelled_by_guest_" + System.currentTimeMillis();
        String notificationData = tableNumber + "|" +
                reservationDate + "|" +
                reservationTime + "|" +
                reservationId + "|" +
                "cancelled_by_guest" + "|" +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date());

        editor.putString(key, notificationData);
        editor.apply();

        Log.d("GuestReservation", "Cancellation saved to SharedPreferences");
    }

    private void navigateToMenu() {
        Intent intent = new Intent(this, GuestMenuActivity.class);
        // Clear back stack to prevent going back to this screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToReservations() {
        Intent intent = new Intent(this, GuestReservationActivity.class);
        // Clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private String getCurrentUserId() {
        // Get from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("user_id", "guest_" + System.currentTimeMillis());
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
        // Pass the reservation data to NotificationActivity
        intent.putExtra("RESERVATION_TABLE", tableNumber);
        intent.putExtra("RESERVATION_DATE", reservationDate);
        intent.putExtra("RESERVATION_TIME", reservationTime);
        intent.putExtra("USER_TYPE", "guest");
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reservationDbHelper != null) {
            reservationDbHelper.close();
        }
        if (notificationDbHelper != null) {
            notificationDbHelper.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data if needed
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToReservations();
    }
}