package com.example.financemanagerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadUser(); // should happen in the welcome activity, failsafe

        // Load the SettingsFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

        // set up the buttons across the top to save whatever is currently on the screen and to redirect to the appropriate activity
        // instantiate all buttons and disable the button for the current activity. For the rest, set up the change activity
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(SettingsActivity.this, WelcomePageActivity.class);
            startActivity(intent);
        });
        Button accountManagerButton = findViewById(R.id.accountManagerButton);
        accountManagerButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(SettingsActivity.this, AccountManagerActivity.class);
            startActivity(intent);
        });
        Button netWorthButton = findViewById(R.id.netWorthButton);
        netWorthButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(SettingsActivity.this, NetWorthCalculator.class);
            startActivity(intent);
        });

        Button goalTrackerButton = findViewById(R.id.goalTrackerButton);
        goalTrackerButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(SettingsActivity.this, GoalTrackerActivity.class);
            startActivity(intent);
        });
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setEnabled(false);

        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(v -> {
            savePage();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> {
            helpPopup();
        });

        Button deleteUserButton = findViewById(R.id.deleteUserButton);
        deleteUserButton.setOnClickListener(v -> {
            confirmDeletion_Popup();
        });
    }

    // popup to confirm deleting the account
    private void confirmDeletion_Popup()
    {

        // Inflate the custom layout for the dialog
        LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_confirm_deletion, null);
        CheckBox checkBox = dialogLayout.findViewById(R.id.confirmDeleteCheckBox);

        // create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout)
                .setTitle(getResources().getString(R.string.confirmDeletePopup_Title))
                .setPositiveButton("Delete", null) // Set later to enable/disable
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Disable the positive button initially
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        // Enable positive button only if checkbox is checked
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                positiveButton.setEnabled(isChecked);
            }
        });

        // Set the positive button action
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount(); // Call your delete method
                dialog.dismiss();
            }
        });
    }


    // deletes the user's account from FireStore
    private void deleteAccount()
    {
        // save all user data to the firebase
        FirebaseUser auth = mAuth.getCurrentUser();
        if(auth != null) {
            String userID = auth.getUid();

            db.collection("users").document(userID)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(SettingsActivity.this, "Account deleted successfully!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SettingsActivity.this, "Error deleting account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("DeleteUser", "User is not authenticated");
        }

        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // creates a popup with info on how to navigate the current activity
    private void helpPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.helpPopupTitle));
        builder.setMessage(getResources().getString(R.string.helpPopupInfo_NetWorth));

        // set a neutral button to dismiss the message
        builder.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());

        // show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // saves the user data to firebase asynchronously (.set method is async by nature)
    private void savePage()
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

    // loads the user at the start of the activity to make sure all data is there (failsafe from when the data should have been loaded in the welcome activity)
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
                    Log.d("loadUser", "Loading the user data from the firebase");
                }
            });
        }
    }
}