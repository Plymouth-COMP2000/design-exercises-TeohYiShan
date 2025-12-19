package com.example.restaurantmanagementapplicationse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MenuDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "restaurant_menu.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_MENU_ITEMS = "menu_items";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_IMAGE = "image";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";

    // Create table SQL
    private static final String CREATE_TABLE_MENU_ITEMS =
            "CREATE TABLE " + TABLE_MENU_ITEMS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_PRICE + " REAL NOT NULL," +
                    COLUMN_DESCRIPTION + " TEXT," +
                    COLUMN_IMAGE + " TEXT," + // Store image as Base64 string
                    COLUMN_CATEGORY + " TEXT DEFAULT 'Meals'," +
                    COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    COLUMN_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    public MenuDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MENU_ITEMS);

        // Insert sample menu items WITH IMAGES
        insertSampleMenuItems(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU_ITEMS);
        onCreate(db);
    }

    private void insertSampleMenuItems(SQLiteDatabase db) {
        // Sample menu items with descriptions and Base64 image strings
        String[] sampleItems = {
                // Format: Name, Price, Description, Category, Base64Image
                "Grilled Salmon|12.99|Fresh Atlantic salmon with herbs and lemon|Meals|" + getSampleImageBase64("salmon"),
                "Beef Steak|15.99|Premium beef steak with mashed potatoes|Meals|" + getSampleImageBase64("steak"),
                "Caesar Salad|8.99|Fresh romaine lettuce with Caesar dressing|Salads|" + getSampleImageBase64("salad"),
                "Chocolate Cake|6.99|Rich chocolate cake with ganache|Desserts|" + getSampleImageBase64("cake"),
                "Iced Tea|3.99|Refreshing iced tea with lemon|Drinks|" + getSampleImageBase64("tea"),
                "Spaghetti Carbonara|11.99|Classic Italian pasta with bacon and egg|Meals|" + getSampleImageBase64("pasta"),
                "Mushroom Soup|5.99|Creamy mushroom soup with herbs|Starters|" + getSampleImageBase64("soup"),
                "Cheeseburger|9.99|Juicy beef patty with cheese and vegetables|Meals|" + getSampleImageBase64("burger"),
                "French Fries|4.99|Crispy golden fries with ketchup|Sides|" + getSampleImageBase64("fries"),
                "Orange Juice|3.49|Freshly squeezed orange juice|Drinks|" + getSampleImageBase64("juice")
        };

        for (String itemData : sampleItems) {
            String[] parts = itemData.split("\\|");
            if (parts.length >= 4) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, parts[0]);
                values.put(COLUMN_PRICE, Double.parseDouble(parts[1]));
                values.put(COLUMN_DESCRIPTION, parts[2]);
                values.put(COLUMN_CATEGORY, parts[3]);

                // Add image if provided (5th part)
                if (parts.length >= 5 && !parts[4].equals("null")) {
                    values.put(COLUMN_IMAGE, parts[4]);
                }

                db.insert(TABLE_MENU_ITEMS, null, values);
                Log.d("MenuDatabaseHelper", "Inserted sample item: " + parts[0]);
            }
        }
    }

    // Method to create sample Base64 images (simplified version)
    private String getSampleImageBase64(String foodType) {
        // In a real app, you would load actual images from drawable resources
        // For now, we'll create a simple colored bitmap as placeholder

        try {
            // Create a simple colored bitmap based on food type
            Bitmap bitmap = createColoredBitmap(foodType);
            return bitmapToBase64(bitmap);
        } catch (Exception e) {
            Log.e("MenuDatabaseHelper", "Error creating sample image: " + e.getMessage());
            return null;
        }
    }

    // Create a simple colored bitmap for sample items
    private Bitmap createColoredBitmap(String foodType) {
        // Different colors for different food types
        int color = getColorForFoodType(foodType);

        // Create a simple 100x100 bitmap with the color
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(color);
        canvas.drawRect(0, 0, 100, 100, paint);

        // Add text to identify the food
        paint.setColor(android.graphics.Color.WHITE);
        paint.setTextSize(20);
        paint.setTextAlign(android.graphics.Paint.Align.CENTER);
        canvas.drawText(foodType, 50, 50, paint);

        return bitmap;
    }

    // Get color based on food type
    private int getColorForFoodType(String foodType) {
        switch (foodType.toLowerCase()) {
            case "salmon": return 0xFFFF6B6B; // Red
            case "steak": return 0xFFC44536;  // Dark Red
            case "salad": return 0xFF51CF66;  // Green
            case "cake": return 0xFFFFD93D;   // Yellow
            case "tea": return 0xFF339AF0;    // Blue
            case "pasta": return 0xFF9775FA;  // Purple
            case "soup": return 0xFF20C997;   // Teal
            case "burger": return 0xFFF76707; // Orange
            case "fries": return 0xFFFF922B;  // Light Orange
            case "juice": return 0xFFF06595;  // Pink
            default: return 0xFF868E96;       // Gray
        }
    }

    // Convert Bitmap to Base64 string
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // Compress quality 80%
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Convert Base64 String to Bitmap
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;

        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("MenuDatabaseHelper", "Error converting base64 to bitmap: " + e.getMessage());
            return null;
        }
    }

    // Add a new menu item
    public long addMenuItem(MenuItem menuItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, menuItem.getName());
        values.put(COLUMN_PRICE, menuItem.getPrice());
        values.put(COLUMN_DESCRIPTION, menuItem.getDescription());
        values.put(COLUMN_CATEGORY, menuItem.getCategory());

        // Add image if exists
        if (menuItem.getImageBase64() != null && !menuItem.getImageBase64().isEmpty()) {
            values.put(COLUMN_IMAGE, menuItem.getImageBase64());
        }

        long id = db.insert(TABLE_MENU_ITEMS, null, values);
        db.close();
        return id;
    }

    // Get all menu items
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MENU_ITEMS + " ORDER BY " + COLUMN_CATEGORY + ", " + COLUMN_NAME;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                MenuItem menuItem = new MenuItem();
                menuItem.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                menuItem.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                menuItem.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                menuItem.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));

                // Get image if exists
                String imageBase64 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
                menuItem.setImageBase64(imageBase64);

                menuItem.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                menuItem.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
                menuItem.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)));

                menuItems.add(menuItem);
                Log.d("MenuDatabaseHelper", "Loaded item: " + menuItem.getName() +
                        ", Image: " + (imageBase64 != null ? "Yes" : "No"));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return menuItems;
    }

    // Get menu items by category
    public List<MenuItem> getMenuItemsByCategory(String category) {
        List<MenuItem> menuItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MENU_ITEMS +
                " WHERE " + COLUMN_CATEGORY + " = ? ORDER BY " + COLUMN_NAME;
        Cursor cursor = db.rawQuery(query, new String[]{category});

        if (cursor.moveToFirst()) {
            do {
                MenuItem menuItem = new MenuItem();
                menuItem.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                menuItem.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                menuItem.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                menuItem.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                menuItem.setImageBase64(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)));
                menuItem.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));

                menuItems.add(menuItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return menuItems;
    }

    // Get single menu item by ID
    public MenuItem getMenuItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MENU_ITEMS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PRICE, COLUMN_DESCRIPTION,
                        COLUMN_IMAGE, COLUMN_CATEGORY, COLUMN_CREATED_AT, COLUMN_UPDATED_AT},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        MenuItem menuItem = null;
        if (cursor != null && cursor.moveToFirst()) {
            menuItem = new MenuItem();
            menuItem.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            menuItem.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            menuItem.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
            menuItem.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
            menuItem.setImageBase64(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)));
            menuItem.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
            menuItem.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
            menuItem.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)));

            cursor.close();
        }

        db.close();
        return menuItem;
    }

    // Update menu item
    public boolean updateMenuItem(MenuItem menuItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, menuItem.getName());
        values.put(COLUMN_PRICE, menuItem.getPrice());
        values.put(COLUMN_DESCRIPTION, menuItem.getDescription());
        values.put(COLUMN_CATEGORY, menuItem.getCategory());

        // Update image if exists
        if (menuItem.getImageBase64() != null && !menuItem.getImageBase64().isEmpty()) {
            values.put(COLUMN_IMAGE, menuItem.getImageBase64());
        }

        int rowsAffected = db.update(TABLE_MENU_ITEMS, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(menuItem.getId())});
        db.close();

        return rowsAffected > 0;
    }

    // Delete menu item
    public void deleteMenuItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MENU_ITEMS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete all menu items
    public void deleteAllMenuItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MENU_ITEMS, null, null);
        db.close();
    }

    // Get categories
    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT " + COLUMN_CATEGORY + " FROM " + TABLE_MENU_ITEMS +
                " ORDER BY " + COLUMN_CATEGORY;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return categories;
    }

    // Get item count
    public int getMenuItemCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_MENU_ITEMS;
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    // Optional: Clear database and reinsert samples
    public void resetToSampleData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MENU_ITEMS, null, null);
        insertSampleMenuItems(db);
        db.close();
        Log.d("MenuDatabaseHelper", "Reset to sample data");
    }
}