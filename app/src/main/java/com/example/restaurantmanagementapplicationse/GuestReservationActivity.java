package com.example.restaurantmanagementapplicationse;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GuestReservationActivity extends AppCompatActivity {

    private Spinner tableNoSpinner;
    private EditText dateEditText;
    private Spinner timeSpinner;
    private Button saveButton;
    private ImageButton logoutButton, notificationButton;
    private TextView reservationTitle;
    private Calendar selectedCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_reservation);

        selectedCalendar = Calendar.getInstance();

        initializeViews();
        setupSpinners();
        setupClickListeners();
        setupNavigation();
    }

    private void initializeViews() {
        tableNoSpinner = findViewById(R.id.tableNoSpinner);
        dateEditText = findViewById(R.id.dateEditText);
        timeSpinner = findViewById(R.id.timeSpinner);
        saveButton = findViewById(R.id.saveButton);

        // Header components
        logoutButton = findViewById(R.id.logoutButton);
        notificationButton = findViewById(R.id.notificationButton);
        reservationTitle = findViewById(R.id.reservationTitle);
    }

    private void setupSpinners() {
        // Set up table number spinner
        ArrayAdapter<CharSequence> tableAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.table_numbers,
                android.R.layout.simple_spinner_item
        );
        tableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tableNoSpinner.setAdapter(tableAdapter);

        // Set initial selection to first item
        tableNoSpinner.setSelection(0);

        // Set up time spinner
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.time_slots,
                android.R.layout.simple_spinner_item
        );
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        // Set initial selection to first item
        timeSpinner.setSelection(0);
    }

    private void setupClickListeners() {
        // Date picker
        dateEditText.setOnClickListener(v -> showDatePicker());

        // Date field focus
        dateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker();
            }
        });

        // Save button
        saveButton.setOnClickListener(v -> {
            if (validateForm()) {
                saveReservationToDatabase();
            }
        });

        // Header buttons
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> logoutUser());
        }

        if (notificationButton != null) {
            notificationButton.setOnClickListener(v -> openNotifications());
        }
    }

    private void setupNavigation() {
        // Bottom navigation
        LinearLayout menuTab = findViewById(R.id.menuTab);
        LinearLayout reservationsTab = findViewById(R.id.reservationsTab);

        menuTab.setOnClickListener(v -> {
            Intent intent = new Intent(GuestReservationActivity.this, GuestMenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        reservationsTab.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Reservations", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dateEditText.setText(dateFormat.format(selectedCalendar.getTime()));
                },
                year, month, day
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        // Set maximum date
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 3);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.setTitle("Select Reservation Date");
        datePickerDialog.show();
    }

    private boolean validateForm() {
        String tableNo = tableNoSpinner.getSelectedItem().toString();
        String date = dateEditText.getText().toString().trim();
        String time = timeSpinner.getSelectedItem().toString();

        if (tableNo.equals("Select Table")) {
            Toast.makeText(this, "Please select a table", Toast.LENGTH_SHORT).show();
            tableNoSpinner.requestFocus();
            return false;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            dateEditText.requestFocus();
            return false;
        }

        if (time.equals("Select Time")) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            timeSpinner.requestFocus();
            return false;
        }

        // Check if selected time is in the future
        if (selectedCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveReservationToDatabase() {
        String tableNo = extractTableNumber(tableNoSpinner.getSelectedItem().toString());
        String date = dateEditText.getText().toString();
        String time = timeSpinner.getSelectedItem().toString();

        // Get current user ID
        String guestUserId = getCurrentUserId();

        // Generate reservation ID
        String reservationId = "RES_" + System.currentTimeMillis();

        // 1. Save to database
        ReservationDatabaseHelper dbHelper = new ReservationDatabaseHelper(this);
        long result = dbHelper.addReservation(tableNo, date, time, guestUserId, reservationId);

        if (result != -1) {
            Log.d("GuestReservation", "Reservation saved to DB with ID: " + reservationId);

            // 2. Create notifications
            createStaffNotification(tableNo, date, time, reservationId);
            createGuestNotification(tableNo, date, time, reservationId, "confirmed");

            // 3. Store in SharedPreferences
            ReservationData.getInstance().setReservationData(tableNo, date, time, reservationId);
            saveToNotifications(tableNo, date, time, reservationId);

            Toast.makeText(this,
                    "Reservation saved for Table " + tableNo + " on " + date + " at " + time,
                    Toast.LENGTH_LONG).show();

            // 4. Navigate to success screen WITH THE RESERVATION ID
            navigateToSuccessScreen(tableNo, date, time, reservationId);
        } else {
            Toast.makeText(this, "Failed to save reservation", Toast.LENGTH_SHORT).show();
        }
    }

    private void createStaffNotification(String tableNo, String date, String time, String reservationId) {
        String notificationId = "NOTIF_STAFF_" + System.currentTimeMillis();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        String title = "New Reservation";
        String message = "New reservation for Table " + tableNo +
                " on " + date + " at " + time;

        Notification notification = new Notification(
                notificationId,
                title,
                message,
                tableNo,
                date,
                time,
                "new_reservation",
                timestamp,
                reservationId
        );

        NotificationDatabaseHelper notificationDbHelper = new NotificationDatabaseHelper(this);
        notificationDbHelper.addNotification(notification, "staff_user");
    }

    private void createGuestNotification(String tableNo, String date, String time,
                                         String reservationId, String type) {
        String notificationId = "NOTIF_GUEST_" + System.currentTimeMillis();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        String title = "Reservation Confirmed";
        String message = "Your reservation for Table " + tableNo +
                " on " + date + " at " + time + " is confirmed!";

        Notification notification = new Notification(
                notificationId,
                title,
                message,
                tableNo,
                date,
                time,
                "confirmed",
                timestamp,
                reservationId
        );

        NotificationDatabaseHelper notificationDbHelper = new NotificationDatabaseHelper(this);
        String guestUserId = getCurrentUserId();
        long result = notificationDbHelper.addNotification(notification, guestUserId);
        Log.d("GuestReservation", "Guest notification saved, result: " + result);
    }

    private String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("user_id", "guest_" + System.currentTimeMillis());
    }

    private void saveToNotifications(String tableNo, String date, String time, String reservationId) {
        SharedPreferences prefs = getSharedPreferences("reservation_notifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save the latest reservation
        editor.putString("latest_table", tableNo);
        editor.putString("latest_date", date);
        editor.putString("latest_time", time);
        editor.putString("latest_reservation_id", reservationId);
        editor.putLong("latest_timestamp", System.currentTimeMillis());

        // Also add to notification history
        String historyKey = "notification_" + System.currentTimeMillis();
        String notificationData = tableNo + "|" + date + "|" + time + "|" + reservationId;
        editor.putString(historyKey, notificationData);

        editor.apply();
    }

    private String extractTableNumber(String tableText) {
        return tableText.replaceAll("[^0-9]", "");
    }

    private void navigateToSuccessScreen(String tableNo, String date, String time, String reservationId) {
        Intent intent = new Intent(this, GuestReservationSuccessActivity.class);
        intent.putExtra("TABLE_NUMBER", extractTableNumber(tableNo));
        intent.putExtra("RESERVATION_DATE", date);
        intent.putExtra("RESERVATION_TIME", time);
        intent.putExtra("RESERVATION_ID", reservationId);
        startActivity(intent);
        finish();
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
        intent.putExtra("USER_TYPE", "guest");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check for any pending notifications
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, GuestMenuActivity.class);
        startActivity(intent);
        finish();
    }
}