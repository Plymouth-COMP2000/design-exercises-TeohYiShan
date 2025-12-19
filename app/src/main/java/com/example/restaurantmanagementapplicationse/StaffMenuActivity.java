package com.example.restaurantmanagementapplicationse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StaffMenuActivity extends AppCompatActivity {

    // UI Components
    private ImageButton logoutButton, notificationButton;
    private TextView menuTitle, mealsTitle;
    private Button addMenuItemButton;
    private LinearLayout menuTab, reservationsTab;
    private NestedScrollView scrollContentLayout;
    private LinearLayout menuItemsContainer;

    // Database
    private MenuDatabaseHelper dbHelper;
    private List<MenuItem> menuItems;

    // For editing
    private int editingItemId = -1;
    private CardView editingCardView = null;

    // For image selection
    private static final int PICK_IMAGE_REQUEST = 1;
    private Bitmap selectedImageBitmap = null;
    private String selectedImageBase64 = null;
    private boolean isImageSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_menu);

        Log.d("StaffMenuActivity", "=== STAFF MENU ACTIVITY STARTED ===");

        try {
            // Initialize database
            dbHelper = new MenuDatabaseHelper(this);
            Log.d("StaffMenuActivity", "Database helper initialized");

            initializeViews();
            loadMenuItemsFromDatabase();
            setupClickListeners();
            setupNavigation();

            Log.d("StaffMenuActivity", "Setup complete");
        } catch (Exception e) {
            Log.e("StaffMenuActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            // Header
            logoutButton = findViewById(R.id.logoutButton);
            notificationButton = findViewById(R.id.notificationButton);
            menuTitle = findViewById(R.id.menuTitle);

            // Content
            mealsTitle = findViewById(R.id.mealsTitle);
            scrollContentLayout = findViewById(R.id.scrollContentLayout);
            menuItemsContainer = findViewById(R.id.menuItemsContainer);

            // Buttons
            addMenuItemButton = findViewById(R.id.addMenuItemButton);

            // Navigation
            menuTab = findViewById(R.id.menuTab);
            reservationsTab = findViewById(R.id.reservationsTab);

            Log.d("StaffMenuActivity", "All views initialized successfully");
        } catch (Exception e) {
            Log.e("StaffMenuActivity", "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =================== AUTO-REFRESH METHODS ===================

    public void refreshMenu() {
        Log.d("StaffMenuActivity", "Refreshing menu...");
        loadMenuItemsFromDatabase();
    }

    private void loadMenuItemsFromDatabase() {
        // Clear existing views from container
        if (menuItemsContainer != null) {
            menuItemsContainer.removeAllViews();
        } else {
            Log.e("StaffMenuActivity", "menuItemsContainer is null!");
            return;
        }

        // Load from database
        menuItems = dbHelper.getAllMenuItems();

        if (menuItems.isEmpty()) {
            showEmptyState();
            return;
        }

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

        Log.d("StaffMenuActivity", "Menu refreshed with " + menuItems.size() + " items");
    }

    // =================== IMAGE HANDLING METHODS ===================

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                // Get the image URI
                android.net.Uri imageUri = data.getData();

                // Convert URI to Bitmap
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                selectedImageBitmap = BitmapFactory.decodeStream(inputStream);

                // Convert Bitmap to Base64
                selectedImageBase64 = MenuDatabaseHelper.bitmapToBase64(selectedImageBitmap);
                isImageSelected = true;

                Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                Log.e("StaffMenuActivity", "Error loading image: " + e.getMessage());
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // =================== ADD MENU ITEM DIALOG WITH IMAGE ===================

    private void showAddMenuItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Menu Item");

        // Inflate dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_menu_item, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);
        Button btnAddImage = dialogView.findViewById(R.id.btnAddImage);
        ImageView ivPreview = dialogView.findViewById(R.id.ivPreview);

        // Reset image selection
        selectedImageBitmap = null;
        selectedImageBase64 = null;
        isImageSelected = false;
        ivPreview.setVisibility(View.GONE);

        // Image button - open image picker
        btnAddImage.setOnClickListener(v -> {
            openImagePicker();
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String category = etCategory.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Name and price are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                if (category.isEmpty()) category = "Meals";

                // Create new menu item with image
                MenuItem newItem = new MenuItem(name, price, description, category);
                if (isImageSelected && selectedImageBase64 != null) {
                    newItem.setImageBase64(selectedImageBase64);
                }

                // Add to database
                long newId = dbHelper.addMenuItem(newItem);

                if (newId != -1) {
                    newItem.setId((int) newId);

                    // REFRESH: Reload the menu
                    refreshMenu();

                    Toast.makeText(this, "Menu item added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // =================== EDIT MENU ITEM WITH IMAGE ===================

    private void convertCardToEditMode(CardView cardView, MenuItem menuItem) {
        // Change card layout to edit mode
        LinearLayout cardContent = cardView.findViewById(R.id.cardContent);
        if (cardContent != null) {
            cardContent.removeAllViews();

            // Inflate edit layout
            LayoutInflater inflater = LayoutInflater.from(this);
            View editView = inflater.inflate(R.layout.staff_menu_item_edit, cardContent, false);

            // Get edit fields
            EditText editName = editView.findViewById(R.id.editName);
            EditText editPrice = editView.findViewById(R.id.editPrice);
            EditText editDescription = editView.findViewById(R.id.editDescription);
            Button btnSelectImage = editView.findViewById(R.id.btnSelectImage);
            ImageView ivEditPreview = editView.findViewById(R.id.ivEditPreview);
            Button btnSave = editView.findViewById(R.id.btnSave);
            Button btnCancel = editView.findViewById(R.id.btnCancel);

            // Reset image selection
            selectedImageBitmap = null;
            selectedImageBase64 = null;
            isImageSelected = false;

            // Set current values
            editName.setText(menuItem.getName());
            editPrice.setText(String.valueOf(menuItem.getPrice()));
            editDescription.setText(menuItem.getDescription());

            // Show current image if exists
            if (menuItem.getImageBase64() != null && !menuItem.getImageBase64().isEmpty()) {
                Bitmap currentBitmap = MenuDatabaseHelper.base64ToBitmap(menuItem.getImageBase64());
                if (currentBitmap != null) {
                    ivEditPreview.setImageBitmap(currentBitmap);
                    ivEditPreview.setVisibility(View.VISIBLE);
                    selectedImageBase64 = menuItem.getImageBase64();
                }
            }

            // Image selection
            btnSelectImage.setOnClickListener(v -> {
                openImagePicker();
            });

            // Save button
            btnSave.setOnClickListener(v -> {
                String newName = editName.getText().toString().trim();
                String priceStr = editPrice.getText().toString().trim();

                if (newName.isEmpty() || priceStr.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double newPrice = Double.parseDouble(priceStr);
                    String newDescription = editDescription.getText().toString().trim();

                    // Update menu item
                    menuItem.setName(newName);
                    menuItem.setPrice(newPrice);
                    menuItem.setDescription(newDescription);

                    // Update image if new one selected
                    if (isImageSelected && selectedImageBase64 != null) {
                        menuItem.setImageBase64(selectedImageBase64);
                    }

                    // Save to database
                    dbHelper.updateMenuItem(menuItem);

                    // REFRESH: Convert back to display mode AND refresh entire menu
                    convertCardToDisplayMode(cardView, menuItem);

                    // Also refresh the entire menu to ensure consistency
                    new android.os.Handler().postDelayed(() -> {
                        refreshMenu();
                    }, 300);

                    Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                }
            });

            // Cancel button
            btnCancel.setOnClickListener(v -> {
                // Reload from database and show display mode
                MenuItem refreshedItem = dbHelper.getMenuItem(menuItem.getId());
                convertCardToDisplayMode(cardView, refreshedItem);
            });

            cardContent.addView(editView);
        }
    }

    private void convertCardToDisplayMode(CardView cardView, MenuItem menuItem) {
        // Reset editing state
        editingItemId = -1;
        editingCardView = null;

        // Reload card with display layout
        LinearLayout cardContent = cardView.findViewById(R.id.cardContent);
        if (cardContent != null) {
            cardContent.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(this);
            View displayView = inflater.inflate(R.layout.staff_menu_item_card, cardContent, false);

            // Set display values
            TextView foodName = displayView.findViewById(R.id.foodName);
            TextView foodPrice = displayView.findViewById(R.id.foodPrice);
            ImageView foodImage = displayView.findViewById(R.id.foodImage);
            ImageButton threeDotButton = displayView.findViewById(R.id.threeDotButton);

            foodName.setText(menuItem.getName());
            foodPrice.setText(menuItem.getFormattedPrice());

            if (menuItem.getImageBase64() != null) {
                Bitmap bitmap = MenuDatabaseHelper.base64ToBitmap(menuItem.getImageBase64());
                if (bitmap != null) {
                    foodImage.setImageBitmap(bitmap);
                }
            }

            threeDotButton.setOnClickListener(v -> showActionMenu(menuItem.getId(), cardView));

            cardContent.addView(displayView);
        }
    }

    // =================== DELETE MENU ITEM WITH REFRESH ===================

    private void deleteMenuItem(int itemId, CardView cardView) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Menu Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteMenuItem(itemId);

                    // REFRESH: Remove from container AND refresh entire menu
                    if (menuItemsContainer != null && cardView.getParent() == menuItemsContainer) {
                        menuItemsContainer.removeView(cardView);
                    }

                    // Also refresh the entire menu
                    new android.os.Handler().postDelayed(() -> {
                        refreshMenu();
                    }, 300);

                    Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // =================== OTHER METHODS (Keep as is) ===================

    private void addMenuItemCard(MenuItem menuItem) {
        // Inflate the card layout
        LayoutInflater inflater = LayoutInflater.from(this);
        CardView cardView = (CardView) inflater.inflate(R.layout.staff_menu_item_card, menuItemsContainer, false);

        // Set item data
        TextView foodName = cardView.findViewById(R.id.foodName);
        TextView foodPrice = cardView.findViewById(R.id.foodPrice);
        ImageView foodImage = cardView.findViewById(R.id.foodImage);
        ImageButton threeDotButton = cardView.findViewById(R.id.threeDotButton);

        foodName.setText(menuItem.getName());
        foodPrice.setText(menuItem.getFormattedPrice());

        // Load image if available
        if (menuItem.getImageBase64() != null && !menuItem.getImageBase64().isEmpty()) {
            Bitmap bitmap = MenuDatabaseHelper.base64ToBitmap(menuItem.getImageBase64());
            if (bitmap != null) {
                foodImage.setImageBitmap(bitmap);
            }
        }

        // Set tag to identify this card's item
        cardView.setTag(menuItem.getId());

        // Three-dot button click
        threeDotButton.setOnClickListener(v -> showActionMenu(menuItem.getId(), cardView));

        // Add to container
        menuItemsContainer.addView(cardView);
    }

    private void showActionMenu(int itemId, CardView cardView) {
        // Create action menu dialog
        String[] actions = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Menu Actions");
        builder.setItems(actions, (dialog, which) -> {
            if (which == 0) {
                // Edit
                editMenuItem(itemId, cardView);
            } else if (which == 1) {
                // Delete
                deleteMenuItem(itemId, cardView);
            }
        });
        builder.show();
    }

    private void editMenuItem(int itemId, CardView cardView) {
        // Get the menu item
        MenuItem menuItem = dbHelper.getMenuItem(itemId);
        if (menuItem == null) return;

        // Set editing state
        editingItemId = itemId;
        editingCardView = cardView;

        // Convert card to edit mode
        convertCardToEditMode(cardView, menuItem);
    }

    private void showEmptyState() {
        if (menuItemsContainer == null) return;

        TextView emptyText = new TextView(this);
        emptyText.setText("No menu items available. Add some!");
        emptyText.setTextSize(16);
        emptyText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        emptyText.setPadding(0, 50, 0, 0);
        emptyText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        menuItemsContainer.addView(emptyText);
    }

    private void setupClickListeners() {
        // Logout
        logoutButton.setOnClickListener(v -> logoutUser());

        // Notifications
        notificationButton.setOnClickListener(v -> openNotifications());

        // Add menu item
        addMenuItemButton.setOnClickListener(v -> showAddMenuItemDialog());
    }

    private void setupNavigation() {
        // Menu tab (already active)
        menuTab.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Menu", Toast.LENGTH_SHORT).show();
        });

        // Reservations tab
        reservationsTab.setOnClickListener(v -> {
            navigateToReservations();
        });
    }

    private void navigateToReservations() {
        Intent intent = new Intent(this, StaffReservationActivity.class);
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
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}