package com.example.restaurantmanagementapplicationse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import java.util.ArrayList;
import java.util.List;

public class GuestMenuActivity extends AppCompatActivity {

    // UI Components
    private ImageButton logoutButton, notificationButton;
    private TextView menuTitle;
    private NestedScrollView scrollView;
    private View menuTab, reservationsTab;
    private LinearLayout menuItemsContainer; // Add this

    // Database
    private MenuDatabaseHelper dbHelper;
    private List<MenuItem> menuItems;

    private static final String TAG = "GuestMenuActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_menu);

        Log.d(TAG, "=== GUEST MENU ACTIVITY STARTED ===");

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("USERNAME");
            String userType = intent.getStringExtra("USER_TYPE");
            boolean fromLogin = intent.getBooleanExtra("FROM_LOGIN", false);

            Log.d(TAG, "Received data - Username: " + username +
                    ", UserType: " + userType +
                    ", FromLogin: " + fromLogin);

            if (fromLogin) {
                Toast.makeText(this, "Welcome Guest: " + username, Toast.LENGTH_SHORT).show();
            }
        }

        try {
            // Initialize database (same as staff menu)
            dbHelper = new MenuDatabaseHelper(this);

            // Initialize views
            initializeViews();

            // Load menu items from database
            loadMenuItemsFromDatabase();

            // Setup click listeners
            setupClickListeners();

            // Check for notifications
            checkForNewNotifications();

            Log.d(TAG, "GuestMenuActivity setup complete");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        Log.d(TAG, "Initializing views...");

        try {
            // Header components
            logoutButton = findViewById(R.id.logoutButton);
            notificationButton = findViewById(R.id.notificationButton);
            menuTitle = findViewById(R.id.menuTitle);

            // Navigation tabs
            menuTab = findViewById(R.id.menuTab);
            reservationsTab = findViewById(R.id.reservationsTab);

            // Menu items container (add this ID to your XML)
            menuItemsContainer = findViewById(R.id.menuItemsContainer);

            Log.d(TAG, "Views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error loading UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMenuItemsFromDatabase() {
        Log.d(TAG, "Loading menu items from database...");

        // Clear existing views
        if (menuItemsContainer != null) {
            menuItemsContainer.removeAllViews();
        } else {
            Log.e(TAG, "menuItemsContainer is null!");
            return;
        }

        // Load from database
        menuItems = dbHelper.getAllMenuItems();

        if (menuItems == null || menuItems.isEmpty()) {
            showEmptyState();
            Log.d(TAG, "No menu items found in database");
            return;
        }

        Log.d(TAG, "Loaded " + menuItems.size() + " menu items from database");

        // Group by category
        List<String> categories = dbHelper.getCategories();

        for (String category : categories) {
            // Add category title
            TextView categoryTitle = new TextView(this);
            categoryTitle.setText(category);
            categoryTitle.setTextSize(20);
            categoryTitle.setTextColor(getResources().getColor(android.R.color.black));
            categoryTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            categoryTitle.setPadding(0, 16, 0, 16);
            menuItemsContainer.addView(categoryTitle);

            // Get items for this category
            List<MenuItem> categoryItems = dbHelper.getMenuItemsByCategory(category);

            for (MenuItem item : categoryItems) {
                addMenuItemCard(item);
            }
        }
    }



    private void addMenuItemCard(MenuItem menuItem) {
        try {
            // Inflate the same card layout as staff menu (but without edit buttons)
            LayoutInflater inflater = LayoutInflater.from(this);
            CardView cardView = (CardView) inflater.inflate(R.layout.staff_menu_item_card, menuItemsContainer, false);

            // Set item data
            TextView foodName = cardView.findViewById(R.id.foodName);
            TextView foodPrice = cardView.findViewById(R.id.foodPrice);
            ImageView foodImage = cardView.findViewById(R.id.foodImage);

            // Hide the three-dot menu button for guests
            ImageButton threeDotButton = cardView.findViewById(R.id.threeDotButton);
            if (threeDotButton != null) {
                threeDotButton.setVisibility(View.GONE);
            }

            foodName.setText(menuItem.getName());
            foodPrice.setText(menuItem.getFormattedPrice());

            // Load image if available
            if (menuItem.getImageBase64() != null && !menuItem.getImageBase64().isEmpty()) {
                Bitmap bitmap = MenuDatabaseHelper.base64ToBitmap(menuItem.getImageBase64());
                if (bitmap != null) {
                    foodImage.setImageBitmap(bitmap);
                }
            }

            // Make entire card clickable to show details
            cardView.setOnClickListener(v -> showMenuItemDetails(menuItem));

            // Add to container
            menuItemsContainer.addView(cardView);

        } catch (Exception e) {
            Log.e(TAG, "Error adding menu item card: " + e.getMessage());
        }
    }

    private void showMenuItemDetails(MenuItem menuItem) {
        Log.d(TAG, "Showing menu item details: " + menuItem.getName());

        // Show details dialog or open a detail activity
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(menuItem.getName());

        // Inflate custom layout for details
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_menu_item_details, null);

        TextView tvPrice = dialogView.findViewById(R.id.tvPrice);
        TextView tvDescription = dialogView.findViewById(R.id.tvDescription);
        ImageView ivFood = dialogView.findViewById(R.id.ivFood);

        tvPrice.setText(menuItem.getFormattedPrice());
        tvDescription.setText(menuItem.getDescription());

        // Load image
        if (menuItem.getImageBase64() != null && !menuItem.getImageBase64().isEmpty()) {
            Bitmap bitmap = MenuDatabaseHelper.base64ToBitmap(menuItem.getImageBase64());
            if (bitmap != null) {
                ivFood.setImageBitmap(bitmap);
            }
        }

        builder.setView(dialogView);
        builder.setPositiveButton("OK", null);

        // Optional: Add order button for guests
        builder.setNeutralButton("Add to Cart", (dialog, which) -> {
            // Implement add to cart functionality
            Toast.makeText(this, menuItem.getName() + " added to cart", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    private void showEmptyState() {
        if (menuItemsContainer == null) return;

        TextView emptyText = new TextView(this);
        emptyText.setText("Menu is currently empty. Please check back later!");
        emptyText.setTextSize(16);
        emptyText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        emptyText.setPadding(0, 50, 0, 0);
        emptyText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        menuItemsContainer.addView(emptyText);
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners...");

        try {
            // Logout button
            if (logoutButton != null) {
                logoutButton.setOnClickListener(v -> performLogout());
            }

            // Notification button
            if (notificationButton != null) {
                notificationButton.setOnClickListener(v -> openNotifications());
            }

            // Navigation - Menu tab
            if (menuTab != null) {
                menuTab.setOnClickListener(v -> {
                    Toast.makeText(GuestMenuActivity.this, "You're already on Menu", Toast.LENGTH_SHORT).show();
                });
            }

            // Navigation - Reservations tab
            if (reservationsTab != null) {
                reservationsTab.setOnClickListener(v -> navigateToReservations());
            }

            Log.d(TAG, "Click listeners setup complete");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage());
        }
    }

    private void performLogout() {
        // Clear user session
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        preferences.edit().clear().apply();

        // Navigate to login screen
        Intent intent = new Intent(GuestMenuActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void openNotifications() {
        Intent intent = new Intent(GuestMenuActivity.this, NotificationActivity.class);
        intent.putExtra("USER_TYPE", "guest");
        startActivity(intent);
    }

    private void navigateToReservations() {
        Log.d(TAG, "Navigating to reservations...");
        Intent intent = new Intent(GuestMenuActivity.this, GuestReservationActivity.class);
        startActivity(intent);
    }

    private void checkForNewNotifications() {
        SharedPreferences prefs = getSharedPreferences("reservation_notifications", MODE_PRIVATE);
        long latestTimestamp = prefs.getLong("latest_timestamp", 0);

        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        if (latestTimestamp > twentyFourHoursAgo && notificationButton != null) {
            notificationButton.setContentDescription("Notifications (New)");
            // Optional: Add a badge indicator
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity resumed");

        // Reload menu items when returning to activity
        loadMenuItemsFromDatabase();
        checkForNewNotifications();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back button pressed");
        // Confirm logout on back press
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }
}