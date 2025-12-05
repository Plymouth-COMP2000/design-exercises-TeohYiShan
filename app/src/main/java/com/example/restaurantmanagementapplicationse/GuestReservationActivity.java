package com.example.restaurantmanagementapplicationse;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class GuestReservationActivity extends AppCompatActivity {

    private Spinner tableNoSpinner;
    private EditText dateEditText;
    private Spinner timeSpinner;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_reservation);

        // Initialize views
        tableNoSpinner = findViewById(R.id.tableNoSpinner);
        dateEditText = findViewById(R.id.dateEditText);
        timeSpinner = findViewById(R.id.timeSpinner);
        saveButton = findViewById(R.id.saveButton);

        // Set up table number spinner
        ArrayAdapter<CharSequence> tableAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.table_numbers,
                android.R.layout.simple_spinner_item
        );
        tableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tableNoSpinner.setAdapter(tableAdapter);

        // Set up date picker
        dateEditText.setOnClickListener(v -> showDatePicker());

        // Set up time spinner
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.time_slots,
                android.R.layout.simple_spinner_item
        );
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        // Save button click listener
        saveButton.setOnClickListener(v -> {
            saveReservation();
        });

        // Bottom navigation click listeners
        LinearLayout menuTab = findViewById(R.id.menuTab);
        LinearLayout reservationsTab = findViewById(R.id.reservationsTab);

        menuTab.setOnClickListener(v -> {
            // Navigate to menu screen
            Intent intent = new Intent(GuestReservationActivity.this, GuestMenuActivity.class);
            startActivity(intent);
        });

        reservationsTab.setOnClickListener(v -> {
            // Already on reservations screen
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
                    // Format: DD/MM/YYYY
                    String selectedDate = String.format("%02d/%02d/%04d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    dateEditText.setText(selectedDate);
                },
                year, month, day
        );

        // Optional: Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void saveReservation() {
        String tableNo = tableNoSpinner.getSelectedItem().toString();
        String date = dateEditText.getText().toString();
        String time = timeSpinner.getSelectedItem().toString();

        if (tableNo.equals("Select Table") || date.isEmpty() || time.equals("Select Time")) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save reservation logic here
        // You can get the selected values:
        // - Table number: tableNo
        // - Date: date
        // - Time: time

        Toast.makeText(this, "Reservation saved for Table " + tableNo + " on " + date + " at " + time, Toast.LENGTH_LONG).show();

        // Clear form or navigate somewhere
        // tableNoSpinner.setSelection(0);
        // dateEditText.setText("");
        // timeSpinner.setSelection(0);
    }
}
