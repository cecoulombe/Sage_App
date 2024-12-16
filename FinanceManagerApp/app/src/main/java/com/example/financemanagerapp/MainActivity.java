package com.example.financemanagerapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        manageDarkMode();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // get the textedit values
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        // set up buttons
        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmail();
                String password = getPassword();
                if(!email.matches("") && !password.matches(""))
                {
                    signIn(email, password);
                } else {
                    Toast.makeText(MainActivity.this, "Enter an email and password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmail();
                String password = getPassword();
                if(!email.matches("") && !password.matches(""))
                {
                    createAccount(email, password);
                } else {
                    Toast.makeText(MainActivity.this, "Enter an email and password.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        ImageButton helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> {
            helpPopup();
        });
    }

    // on app start, check if the user is signed in and redirect as needed
    @Override
    public void onStart()
    {
        super.onStart();

        // check if the user is signed in (non-null) and redirect
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            // open the homepage activity
        }
    }

    // returns the entered email
    private String getEmail()
    {
        return emailInput.getText().toString();
    }

    // returns the entered password
    private String getPassword()
    {
        return passwordInput.getText().toString();
    }

    // sign in an existing user
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // sign-in success
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(this, "Sign in successful.", Toast.LENGTH_SHORT).show();
                launchWelcomePage();
            } else {
                // Check if the error is due to "user not found"
                String errorMessage = "Authentication failed. Incorrect email and password or account does not exist.";
                if (task.getException() instanceof FirebaseAuthException) {
                    FirebaseAuthException e = (FirebaseAuthException) task.getException();
                    if (e.getErrorCode().equals("ERROR_USER_NOT_FOUND")) {
                        errorMessage = "No account found with this email.";
                        // Optionally show a dialog asking the user if they want to create a new account
                    }
                }
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }



    // create an account for a new user
    private void createAccount(String email, String password) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Check if the email is already associated with an account
                if (task.getResult().getSignInMethods().isEmpty()) {
                    // Email is not in use, proceed to create an account
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, innerTask -> {
                        if(innerTask.isSuccessful()) {
                            // Account creation success
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(this, "Account creation successful, signing in to account.", Toast.LENGTH_SHORT).show();
                            launchWelcomePage();
                        } else {
                            // Handle account creation failure
                            String errorMessage = "Account Creation Failed. Ensure email and passcode are valid.";
                            if (innerTask.getException() instanceof FirebaseAuthException) {
                                FirebaseAuthException e = (FirebaseAuthException) innerTask.getException();
                                // Handle specific error codes
                            }
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "This email address is already in use.", Toast.LENGTH_LONG).show();
                }
            } else {
                // Handle fetchSignInMethodsForEmail failure
                Toast.makeText(MainActivity.this, "Error checking email usage.", Toast.LENGTH_LONG).show();
            }
        });
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

    // sign out user (called from pressing the sign out button on the other pages?)
    private void signOut()
    {
        mAuth.signOut();
    }
    
    // launch the next activity on sign in
    private void launchWelcomePage()
    {
        Intent intent = new Intent(MainActivity.this, WelcomePageActivity.class);
        startActivity(intent);
    }

    // creates a popup with info on how to navigate the current activity
    private void helpPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.mainActivityHelp_Disclaimer));
        builder.setMessage(getResources().getString(R.string.mainActivityHelp_Popup));

        // set a neutral button to dismiss the message
        builder.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());

        // show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}