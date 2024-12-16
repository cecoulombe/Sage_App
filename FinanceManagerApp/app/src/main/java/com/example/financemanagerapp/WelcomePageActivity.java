package com.example.financemanagerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

public class WelcomePageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String displayName = "";
    private TextView welcomeMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        manageDarkMode();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // set up the buttons across the top to save whatever is currently on the screen and to redirect to the appropriate activity
        // instantiate all buttons and disable the button for the current activity. For the rest, set up the change activity
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setEnabled(false);
        Button accountManagerButton = findViewById(R.id.accountManagerButton);
        accountManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, AccountManagerActivity.class);
            startActivity(intent);
        });
        Button netWorthButton = findViewById(R.id.netWorthButton);
        netWorthButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, NetWorthCalculator.class);
            startActivity(intent);
        });
        Button goalTrackerButton = findViewById(R.id.goalTrackerButton);
        goalTrackerButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, GoalTrackerActivity.class);
            startActivity(intent);
        });
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(WelcomePageActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // initialize the welcome textview
        welcomeMsg = findViewById(R.id.welcomeTextView);

        // check if a user's document exists
        checkUserDocs();

        // set up the currency exchange button
        ImageButton currencyExchangeButton = findViewById(R.id.currencyExchangeButton);
        currencyExchangeButton.setOnClickListener(v -> {
            // Create an instance of the fragment
            CurrencyExchangeFragment calculatorFragment = new CurrencyExchangeFragment();

            // Show the fragment as a popup
            FragmentManager fragmentManager = getSupportFragmentManager();
            calculatorFragment.show(fragmentManager, "CurrencyExchangeFragment");
        });
    }

    // verifies if the user exists within the database
    private void checkUserDocs()
    {
        FirebaseUser auth = mAuth.getCurrentUser();
        if(auth != null)
        {
            String userID = auth.getUid();

            DocumentReference userRef = db.collection("users").document(userID);

            userRef.get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                if(document.exists())
                {
                    Log.d("Firebase", "User data exists, loading it");
                    loadUser();
                } else {
                    Log.d("Firebase", "User data does not exist, creating a new one");
                    createNewUser();
                }
            });
        }
    }

    // loads the user data for an existing user in the firebase
    private void loadUser()
    {
        FirebaseUser auth = mAuth.getCurrentUser();
        if(auth != null) {
            String userID = auth.getUid();
            DocumentReference docRef = db.collection("users").document(userID);

            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User user = documentSnapshot.toObject(User.class);
                    GlobalUser.setUser(user);
                    Log.d("loadUser", "Loading the user data from the firebase, including name? " + user.getName());
                    getDisplayName();
                }
            });
        }
    }

    // saves the user data to firebase asynchronously (.set method is async by nature)
    private void saveUser()
    {
        // save all user data to the firebase
        FirebaseUser auth = mAuth.getCurrentUser();
        if(auth != null) {
            String userID = auth.getUid();

            db.collection("users").document(userID)
                    .set(GlobalUser.getUser())
                    .addOnSuccessListener(aVoid ->
                    {
                        Log.d("SavePage", "User data saved successfully.");
                    })
                    .addOnFailureListener(e ->
                    {
                        Log.d("SavePage", "Error saving user data: " + e.getMessage());
                    });
        } else {
            Log.e("SavePage", "User is not authenticated");
        }
    }

    // creates a new user
    private void createNewUser()
    {
        promptForNickname();
//        User user = new User(displayName);
//        GlobalUser.setUser(user);
//        saveUser();
//        updateWelcomeMsg();
    }

    // creates an alertDialog which prompts the user for their desired nickname and saves it to firebase
    private void promptForNickname()
    {
        // create an edittext for user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Nickname")
                .setMessage("Please enter your nickname")
                .setView(input) // Set the EditText in the dialog
                .setPositiveButton("Submit", null) // Don't close the dialog immediately
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss()); // Optional: cancel button

        // Create the dialog and get reference to it
        AlertDialog dialog = builder.create();
        Log.d("NicknamePrompt", "Put up the dialog, now need an input");

        // Set up validation when the user clicks "Submit"
        dialog.setOnShowListener(d -> {
            Button submitButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            submitButton.setOnClickListener(v -> {
                // Get the entered nickname
                String nickname = input.getText().toString().trim();
                Log.d("NicknamePrompt", "has an input, need to validate it: " + nickname);
                // Perform validation
                if (nickname.isEmpty()) {
                    // If nickname is empty, show a validation message
                    input.setError("Nickname cannot be empty");
                } else if (nickname.length() < 2) {
                    input.setError("Nickname must be at least 2 characters long");
                } else {
                    // If nickname is valid, save it and dismiss the dialog
                    displayName = nickname;
                    Log.d("NicknamePrompt", "Valid nickname entered, saving to displayName: " + displayName);
                    dialog.dismiss();
                    User user = new User(displayName);
                    GlobalUser.setUser(user);
                    saveUser();
                    updateWelcomeMsg();
                }
            });
        });

        // Show the dialog
        dialog.show();
    }

    // sets the display name from an existing user
    private void getDisplayName()
    {
        displayName = GlobalUser.getUser().getName();
        Log.d("getDisplayName", "Getting the displayName from the user object: " + displayName);
        updateWelcomeMsg();
    }

    // updates the welcome message to include the user's nickname
    private void updateWelcomeMsg()
    {
        Log.d("updateWelcomeMsg", "Updating the welcome msg to include the name: " + displayName);
        String msg = getResources().getString(R.string.welcomeNicknameLabel, displayName);
        welcomeMsg.setText(msg);
    }

    // checks the user prefs and determines whether or not the app should be in dark mode. Applies any changes
    private void manageDarkMode()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false); // Default is false if not set

        // Apply the theme based on the preference
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);
    }

}