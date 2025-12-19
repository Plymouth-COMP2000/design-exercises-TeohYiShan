package com.example.restaurantmanagementapplicationse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView signUpTextView, forgotPasswordTextView;
    private CheckBox rememberMeCheckBox;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;

    // API Configuration
    private static final String API_BASE_URL = "http://10.240.72.69/comp2000/coursework/";
    private static final String STUDENT_ID = "BSSE2506022"; // Your student ID

    // Permission request code
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 100;

    // For debugging
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);

        // Request notification permission for Android 13+ only
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        // Check if already logged in
        if (isUserLoggedIn()) {
            redirectToAppropriateScreen();
            return;
        }

        setContentView(R.layout.login);
        initializeViews();
        setupClickListeners();
        loadSavedCredentials();
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Sign up feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSavedCredentials() {
        boolean rememberMe = sharedPreferences.getBoolean("remember_me", false);

        if (rememberMe) {
            String savedUsername = sharedPreferences.getString("saved_username", "");
            String savedPassword = sharedPreferences.getString("saved_password", "");

            usernameEditText.setText(savedUsername);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Some features may not work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("is_logged_in", false);
    }

    private void performLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save remember me preference
        if (rememberMeCheckBox.isChecked()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("saved_username", username);
            editor.putString("saved_password", password);
            editor.putBoolean("remember_me", true);
            editor.apply();
        } else {
            // Clear saved credentials if not checked
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("saved_username");
            editor.remove("saved_password");
            editor.putBoolean("remember_me", false);
            editor.apply();
        }

        // Show loading
        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");

        // Authenticate with API
        authenticateUser(username, password);
    }

    private void authenticateUser(String username, String password) {
        String url = API_BASE_URL + "read_all_users/" + STUDENT_ID;

        Log.d(TAG, "Authenticating: " + username + " at URL: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "API Response: " + response);

                        try {
                            // Parse the JSON response
                            JSONObject jsonResponse = new JSONObject(response);

                            // Check if response has "users" array
                            if (jsonResponse.has("users")) {
                                JSONArray users = jsonResponse.getJSONArray("users");
                                boolean found = false;
                                JSONObject userData = null;
                                String userType = "";

                                // Search for matching user
                                for (int i = 0; i < users.length(); i++) {
                                    JSONObject user = users.getJSONObject(i);

                                    // Get fields from API response
                                    String apiUsername = user.getString("username");
                                    String apiPassword = user.getString("password");
                                    String apiUserType = user.getString("usertype");

                                    Log.d(TAG, "Checking user: " + apiUsername + ", type: " + apiUserType);

                                    // Check credentials
                                    if (apiUsername.equals(username) && apiPassword.equals(password)) {
                                        found = true;
                                        userData = user;
                                        userType = apiUserType;
                                        break;
                                    }
                                }

                                if (found && userData != null) {
                                    Log.d(TAG, "Authentication successful! User type: " + userType);

                                    // Save user data to SharedPreferences
                                    saveUserData(userData, userType);

                                    // Navigate based on user type
                                    navigateBasedOnUserType(userType);

                                } else {
                                    Log.d(TAG, "Authentication failed: Invalid credentials");
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this,
                                                "Invalid username or password",
                                                Toast.LENGTH_SHORT).show();
                                        resetLoginButton();
                                    });
                                }
                            } else {
                                Log.d(TAG, "API response missing 'users' array");
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this,
                                            "Invalid API response format",
                                            Toast.LENGTH_SHORT).show();
                                    resetLoginButton();
                                });
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this,
                                        "Error parsing response",
                                        Toast.LENGTH_SHORT).show();
                                resetLoginButton();
                            });
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley error: " + error.toString());
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "Network error: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            resetLoginButton();

                            // For testing, use hardcoded credentials
                            if (username.equals("guest") && password.equals("password")) {
                                simulateGuestLogin();
                            } else if (username.equals("staff") && password.equals("password")) {
                                simulateStaffLogin();
                            }
                        });
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void saveUserData(JSONObject userData, String userType) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Save all user fields from API
            editor.putString("user_id", userData.getString("_id"));
            editor.putString("username", userData.getString("username"));
            editor.putString("firstname", userData.getString("firstname"));
            editor.putString("lastname", userData.getString("lastname"));
            editor.putString("email", userData.getString("email"));
            editor.putString("contact", userData.getString("contact"));
            editor.putString("usertype", userType);
            editor.putBoolean("is_logged_in", true);

            // Set role flags
            boolean isStaff = "staff".equalsIgnoreCase(userType);
            editor.putBoolean("is_staff", isStaff);
            editor.putBoolean("is_guest", !isStaff);

            editor.apply();

            Log.d(TAG, "User data saved: " + userData.getString("username"));

            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Log.e(TAG, "Error saving user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateBasedOnUserType(String userType) {
        runOnUiThread(() -> {
            try {
                Log.d(TAG, "Navigating to screen for user type: " + userType);

                Intent intent;

                if ("staff".equalsIgnoreCase(userType)) {
                    Log.d(TAG, "Creating intent for StaffMenuActivity");
                    intent = new Intent(MainActivity.this, StaffMenuActivity.class);
                } else {
                    // Default to guest
                    Log.d(TAG, "Creating intent for GuestMenuActivity");
                    intent = new Intent(MainActivity.this, GuestMenuActivity.class);
                }

                // Pass user data if needed
                intent.putExtra("USERNAME", sharedPreferences.getString("username", ""));
                intent.putExtra("USER_TYPE", userType);

                Log.d(TAG, "Starting activity: " + intent.getComponent().getClassName());
                startActivity(intent);
                Log.d(TAG, "Activity started, finishing MainActivity");
                finish();

            } catch (Exception e) {
                Log.e(TAG, "Navigation error: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(MainActivity.this,
                        "Navigation error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                resetLoginButton();
            }
        });
    }

    private void redirectToAppropriateScreen() {
        String userType = sharedPreferences.getString("usertype", "guest");
        navigateBasedOnUserType(userType);
    }

    private void simulateGuestLogin() {
        Log.d(TAG, "Using simulated guest login");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", "simulated_guest");
        editor.putString("username", "guest");
        editor.putString("firstname", "Guest");
        editor.putString("lastname", "User");
        editor.putString("email", "guest@example.com");
        editor.putString("contact", "0123456789");
        editor.putString("usertype", "guest");
        editor.putBoolean("is_logged_in", true);
        editor.putBoolean("is_staff", false);
        editor.putBoolean("is_guest", true);
        editor.apply();

        navigateBasedOnUserType("guest");
    }

    private void simulateStaffLogin() {
        Log.d(TAG, "Using simulated staff login");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", "simulated_staff");
        editor.putString("username", "staff");
        editor.putString("firstname", "Staff");
        editor.putString("lastname", "User");
        editor.putString("email", "staff@example.com");
        editor.putString("contact", "0123456788");
        editor.putString("usertype", "staff");
        editor.putBoolean("is_logged_in", true);
        editor.putBoolean("is_staff", true);
        editor.putBoolean("is_guest", false);
        editor.apply();

        navigateBasedOnUserType("staff");
    }

    private void resetLoginButton() {
        runOnUiThread(() -> {
            loginButton.setEnabled(true);
            loginButton.setText("Log in");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear password field for security
        if (passwordEditText != null) {
            passwordEditText.setText("");
        }
    }
}