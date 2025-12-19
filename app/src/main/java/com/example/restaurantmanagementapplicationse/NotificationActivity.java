package com.example.restaurantmanagementapplicationse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.CompoundButton;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    // UI Components
    private ImageButton logoutButton, settingsButton;
    private TextView notificationTitle, historyTitle;
    private NestedScrollView scrollView;
    private LinearLayout menuTab, reservationsTab, notificationsContainer;

    // Notification cards arrays
    private CardView[] notificationCards;
    private TextView[] notificationMessages;
    private TextView[] tableNoValues;
    private TextView[] dateValues;
    private TextView[] timeValues;
    private TextView[] statusValues;

    // Database
    private NotificationDatabaseHelper notificationDbHelper;

    // Notification data
    private List<NotificationItem> notifications;
    private boolean isGuestUser = true;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        determineUserType();
        getCurrentUserId();
        initializeDatabase();
        initializeViews();
        initializeArrays();
        loadNotificationsFromDatabase(); // CHANGED: Load from database instead
        populateNotifications();
        setupClickListeners();
        setupNavigation();
    }

    private void determineUserType() {
        // Check user type from SharedPreferences or intent
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        isGuestUser = prefs.getBoolean("is_guest", true);

        // Also check from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER_TYPE")) {
            String userType = intent.getStringExtra("USER_TYPE");
            isGuestUser = "guest".equals(userType);
        }
    }

    private void getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (isGuestUser) {
            currentUserId = prefs.getString("user_id", "guest_default");
        } else {
            currentUserId = "staff_user"; // Staff user ID
        }
    }

    private void initializeDatabase() {
        notificationDbHelper = new NotificationDatabaseHelper(this);
    }

    private void initializeViews() {
        logoutButton = findViewById(R.id.logoutButton);
        settingsButton = findViewById(R.id.settingsButton);
        notificationTitle = findViewById(R.id.notificationTitle);
        historyTitle = findViewById(R.id.historyTitle);
        scrollView = findViewById(R.id.scrollView);
        notificationsContainer = findViewById(R.id.notificationsContainer);
        menuTab = findViewById(R.id.menuTab);
        reservationsTab = findViewById(R.id.reservationsTab);
    }

    private void initializeArrays() {
        // Initialize arrays for 3 notification cards
        notificationCards = new CardView[] {
                findViewById(R.id.notificationCard1),
                findViewById(R.id.notificationCard2),
                findViewById(R.id.notificationCard3)
        };

        notificationMessages = new TextView[] {
                findViewById(R.id.notificationMessage1),
                findViewById(R.id.notificationMessage2),
                findViewById(R.id.notificationMessage3)
        };

        tableNoValues = new TextView[] {
                findViewById(R.id.tableNoValue1),
                findViewById(R.id.tableNoValue2),
                findViewById(R.id.tableNoValue3)
        };

        dateValues = new TextView[] {
                findViewById(R.id.dateValue1),
                findViewById(R.id.dateValue2),
                findViewById(R.id.dateValue3)
        };

        timeValues = new TextView[] {
                findViewById(R.id.timeValue1),
                findViewById(R.id.timeValue2),
                findViewById(R.id.timeValue3)
        };

        statusValues = new TextView[] {
                findViewById(R.id.status1),
                findViewById(R.id.status2),
                findViewById(R.id.status3)
        };
    }

    private void loadNotificationsFromDatabase() {
        notifications = new ArrayList<>();

        // Load from NotificationDatabase
        List<Notification> dbNotifications = notificationDbHelper.getAllNotifications(currentUserId);

        Log.d("NotificationActivity", "Loaded " + dbNotifications.size() + " notifications for user: " + currentUserId);

        for (Notification notification : dbNotifications) {
            NotificationItem item = new NotificationItem(
                    notification.getId(),
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getTableNo(),
                    notification.getDate(),
                    notification.getTime(),
                    notification.getType(),
                    notification.getTimestamp()
            );
            notifications.add(item);
            Log.d("NotificationActivity", "Notification: " + notification.getTitle() +
                    " - Type: " + notification.getType());
        }

        // Also load from SharedPreferences (backup/legacy)
        loadNotificationsFromSharedPreferences();

        // If no notifications found, create sample data
        if (notifications.isEmpty()) {
            createSampleNotifications();
        }

        // Sort by timestamp (newest first)
        notifications.sort((n1, n2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date1 = sdf.parse(n1.getTimestamp());
                Date date2 = sdf.parse(n2.getTimestamp());
                return date2.compareTo(date1); // Newest first
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void loadNotificationsFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("reservation_notifications", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("notification_")) {
                String notificationData = (String) entry.getValue();
                String[] parts = notificationData.split("\\|");

                if (parts.length >= 6) {
                    NotificationItem notification = new NotificationItem(
                            parts[3], // reservationId
                            "Reservation " + (parts[4].equals("confirmed") ? "Confirmed" : parts[4]),
                            "The reservation for",
                            parts[0], // tableNo
                            parts[1], // date
                            parts[2], // time
                            parts[4], // type
                            parts[5]  // timestamp
                    );

                    // Check if already exists in list
                    boolean exists = false;
                    for (NotificationItem item : notifications) {
                        if (item.getId().equals(notification.getId())) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        notifications.add(notification);
                    }
                }
            }
        }
    }

    private void createSampleNotifications() {
        if (isGuestUser) {
            // Sample guest notifications
            notifications.add(new NotificationItem(
                    "RES001",
                    "Reservation Confirmed",
                    "Your reservation has been confirmed",
                    "02",
                    "20/10/2025",
                    "8:00 PM",
                    "confirmed",
                    getFormattedTime(30) // 30 minutes ago
            ));
        } else {
            // Sample staff notifications
            notifications.add(new NotificationItem(
                    "RES101",
                    "New Reservation",
                    "New reservation received",
                    "02",
                    "20/10/2025",
                    "8:00 PM",
                    "new_reservation",
                    getFormattedTime(15) // 15 minutes ago
            ));

            notifications.add(new NotificationItem(
                    "RES102",
                    "Reservation Cancelled",
                    "You cancelled a reservation",
                    "01",
                    "19/10/2025",
                    "7:00 PM",
                    "cancelled",
                    getFormattedTime(45) // 45 minutes ago
            ));
        }

        // Save sample notifications to database
        saveSampleNotificationsToDatabase();
    }

    private void saveSampleNotificationsToDatabase() {
        for (NotificationItem item : notifications) {
            Notification notification = new Notification(
                    item.getId(),
                    item.getTitle(),
                    item.getMessage(),
                    item.getTableNo(),
                    item.getDate(),
                    item.getTime(),
                    item.getType(),
                    item.getTimestamp(),
                    item.getId() // reservationId same as notification id for samples
            );
            notificationDbHelper.addNotification(notification, currentUserId);
        }
    }

    private String getFormattedTime(int minutesAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -minutesAgo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void populateNotifications() {
        if (notifications.isEmpty()) {
            showEmptyState();
            return;
        }

        int maxToShow = Math.min(notifications.size(), notificationCards.length);

        for (int i = 0; i < maxToShow; i++) {
            NotificationItem notification = notifications.get(i);
            updateNotificationCard(i, notification);
            notificationCards[i].setVisibility(View.VISIBLE);
        }

        // Hide unused cards
        for (int i = maxToShow; i < notificationCards.length; i++) {
            notificationCards[i].setVisibility(View.GONE);
        }
    }

    private void updateNotificationCard(int index, NotificationItem notification) {
        if (index >= notificationCards.length) return;

        // Set notification message
        notificationMessages[index].setText(notification.getMessage());

        // Set table number
        if (notification.getTableNo() != null && !notification.getTableNo().isEmpty()) {
            tableNoValues[index].setText(notification.getTableNo());
        }

        // Set date
        if (notification.getDate() != null && !notification.getDate().isEmpty()) {
            dateValues[index].setText(notification.getDate());
        }

        // Set time
        if (notification.getTime() != null && !notification.getTime().isEmpty()) {
            timeValues[index].setText(notification.getTime());
        }

        // Set status
        String statusText = getStatusText(notification.getType());
        statusValues[index].setText(statusText);
        statusValues[index].setTextColor(getStatusColor(notification.getType()));

        // Set click listener
        final int finalIndex = index;
        notificationCards[index].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNotificationClicked(notifications.get(finalIndex));
            }
        });

        // Debug log
        Log.d("NotificationActivity", "Card " + index + ": " + notification.getTitle() +
                " - Type: " + notification.getType());
    }

    private String getStatusText(String type) {
        switch (type) {
            case "confirmed":
                return "Successfully made";
            case "new_reservation":
                return "New Reservation";
            case "modified":
                return "Modified";
            case "cancelled":
            case "cancelled_by_staff":
                return "Cancelled by Staff";
            case "completed":
                return "Completed";
            default:
                return type;
        }
    }

    private int getStatusColor(String type) {
        switch (type) {
            case "confirmed":
            case "new_reservation":
                return getResources().getColor(R.color.success_color);
            case "modified":
                return getResources().getColor(R.color.warning_color);
            case "cancelled":
            case "cancelled_by_staff":
                return getResources().getColor(R.color.error_color);
            default:
                return getResources().getColor(R.color.info_color);
        }
    }

    private void showEmptyState() {
        historyTitle.setVisibility(View.GONE);

        for (CardView card : notificationCards) {
            card.setVisibility(View.GONE);
        }

        TextView emptyMessage = new TextView(this);
        emptyMessage.setText("No notifications yet");
        emptyMessage.setTextSize(18);
        emptyMessage.setTextColor(getResources().getColor(android.R.color.darker_gray));
        emptyMessage.setGravity(View.TEXT_ALIGNMENT_CENTER);
        emptyMessage.setPadding(0, 100, 0, 0);

        notificationsContainer.addView(emptyMessage, 0);
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> logoutUser());
        settingsButton.setOnClickListener(v -> openNotificationSettings());
    }

    private void setupNavigation() {
        menuTab.setOnClickListener(v -> navigateToMenu());
        reservationsTab.setOnClickListener(v -> navigateToReservations());
    }

    private void onNotificationClicked(NotificationItem notification) {
        Toast.makeText(this,
                notification.getTitle() +
                        " - Table " + notification.getTableNo(),
                Toast.LENGTH_SHORT).show();

        // Mark as read in database
        notificationDbHelper.markAsRead(notification.getId());

        // Navigate to reservation details if it's a reservation notification
        if (notification.getTitle().contains("Reservation")) {
            navigateToReservationDetails(notification);
        }
    }

    private void navigateToReservationDetails(NotificationItem notification) {
        if (isGuestUser) {
            Intent intent = new Intent(this, GuestReservationSuccessActivity.class);
            intent.putExtra("TABLE_NUMBER", notification.getTableNo());
            intent.putExtra("RESERVATION_DATE", notification.getDate());
            intent.putExtra("RESERVATION_TIME", notification.getTime());
            startActivity(intent);
        } else {
            // For staff, you could create a StaffReservationDetailActivity
            Toast.makeText(this, "Staff reservation details", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMenu() {
        Intent intent = isGuestUser ?
                new Intent(this, GuestMenuActivity.class) :
                new Intent(this, StaffMenuActivity.class);
        startActivity(intent);
    }

    private void navigateToReservations() {
        Intent intent = isGuestUser ?
                new Intent(this, GuestReservationActivity.class) :
                new Intent(this, StaffReservationActivity.class);
        startActivity(intent);
    }

    private void openNotificationSettings() {
        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notification Settings");

        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notification_settings, null);
        builder.setView(dialogView);

        // Get SharedPreferences
        SharedPreferences prefs = getSharedPreferences("notification_settings", MODE_PRIVATE);

        // Find switches in the dialog
        SwitchCompat switchNewReservations = dialogView.findViewById(R.id.switchNewReservations);
        SwitchCompat switchCancellations = dialogView.findViewById(R.id.switchCancellations);
        SwitchCompat switchSound = dialogView.findViewById(R.id.switchSound);
        SwitchCompat switchVibration = dialogView.findViewById(R.id.switchVibration);
        SwitchCompat switchAllNotifications = dialogView.findViewById(R.id.switchAllNotifications);

        // Load saved settings
        boolean newReservationsEnabled = prefs.getBoolean("new_reservations", true);
        boolean cancellationsEnabled = prefs.getBoolean("cancellations", true);
        boolean soundEnabled = prefs.getBoolean("sound", true);
        boolean vibrationEnabled = prefs.getBoolean("vibration", true);
        boolean allNotificationsEnabled = prefs.getBoolean("all_notifications", true);

        // Set switch states
        switchNewReservations.setChecked(newReservationsEnabled);
        switchCancellations.setChecked(cancellationsEnabled);
        switchSound.setChecked(soundEnabled);
        switchVibration.setChecked(vibrationEnabled);
        switchAllNotifications.setChecked(allNotificationsEnabled);

        // "All Notifications" switch behavior
        switchAllNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // When "All Notifications" is toggled, update all other switches
            switchNewReservations.setChecked(isChecked);
            switchCancellations.setChecked(isChecked);
            switchSound.setChecked(isChecked);
            switchVibration.setChecked(isChecked);

            // Enable/disable other switches based on "All Notifications"
            switchNewReservations.setEnabled(!isChecked);
            switchCancellations.setEnabled(!isChecked);
            switchSound.setEnabled(!isChecked);
            switchVibration.setEnabled(!isChecked);
        });

        // Update "All Notifications" switch when individual switches change
        CompoundButton.OnCheckedChangeListener individualSwitchListener = (buttonView, isChecked) -> {
            boolean allChecked = switchNewReservations.isChecked() &&
                    switchCancellations.isChecked() &&
                    switchSound.isChecked() &&
                    switchVibration.isChecked();
            switchAllNotifications.setChecked(allChecked);
        };

        switchNewReservations.setOnCheckedChangeListener(individualSwitchListener);
        switchCancellations.setOnCheckedChangeListener(individualSwitchListener);
        switchSound.setOnCheckedChangeListener(individualSwitchListener);
        switchVibration.setOnCheckedChangeListener(individualSwitchListener);

        // Set dialog buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Save settings to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("new_reservations", switchNewReservations.isChecked());
            editor.putBoolean("cancellations", switchCancellations.isChecked());
            editor.putBoolean("sound", switchSound.isChecked());
            editor.putBoolean("vibration", switchVibration.isChecked());
            editor.putBoolean("all_notifications", switchAllNotifications.isChecked());
            editor.apply();

            Toast.makeText(NotificationActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();

            // Show summary of enabled notifications
            showNotificationSummary();
        });

        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("Reset to Default", (dialog, which) -> {
            // Reset to default settings
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("new_reservations", true);
            editor.putBoolean("cancellations", true);
            editor.putBoolean("sound", true);
            editor.putBoolean("vibration", true);
            editor.putBoolean("all_notifications", true);
            editor.apply();

            Toast.makeText(NotificationActivity.this, "Reset to default settings", Toast.LENGTH_SHORT).show();
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNotificationSummary() {
        SharedPreferences prefs = getSharedPreferences("notification_settings", MODE_PRIVATE);

        boolean newReservations = prefs.getBoolean("new_reservations", true);
        boolean cancellations = prefs.getBoolean("cancellations", true);
        boolean allNotifications = prefs.getBoolean("all_notifications", true);

        StringBuilder summary = new StringBuilder("Notifications enabled for:\n");

        if (allNotifications) {
            summary.append("• All notification types\n");
        } else {
            if (newReservations) summary.append("• New reservations\n");
            if (cancellations) summary.append("• Cancellations\n");

            if (!newReservations && !cancellations) {
                summary.append("• None (all notifications are off)\n");
            }
        }

        // Show summary in a Toast
        Toast.makeText(this, summary.toString(), Toast.LENGTH_LONG).show();

        // Also log for debugging
        Log.d("NotificationSettings", "New Reservations: " + newReservations);
        Log.d("NotificationSettings", "Cancellations: " + cancellations);
        Log.d("NotificationSettings", "All Notifications: " + allNotifications);
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notifications
        loadNotificationsFromDatabase();
        populateNotifications();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isGuestUser) {
            navigateToMenu();
        } else {
            navigateToReservations();
        }
    }

    // Inner class for notification items
    private static class NotificationItem {
        private String id;
        private String title;
        private String message;
        private String tableNo;
        private String date;
        private String time;
        private String type;
        private String timestamp;

        public NotificationItem(String id, String title, String message, String tableNo,
                                String date, String time, String type, String timestamp) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.tableNo = tableNo;
            this.date = date;
            this.time = time;
            this.type = type;
            this.timestamp = timestamp;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getTableNo() { return tableNo; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getType() { return type; }
        public String getTimestamp() { return timestamp; }
    }
}