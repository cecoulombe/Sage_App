package com.example.financemanagerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountManagerActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TableLayout table;
            
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        manageDarkMode();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_manager);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadUser(); // should happen in the welcome activity, failsafe

        // set up the buttons across the top to save whatever is currently on the screen and to redirect to the appropriate activity
        // instantiate all buttons and disable the button for the current activity. For the rest, set up the change activity
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(AccountManagerActivity.this, WelcomePageActivity.class);
            startActivity(intent);
        });
        Button accountManagerButton = findViewById(R.id.accountManagerButton);
        accountManagerButton.setEnabled(false);

        Button netWorthButton = findViewById(R.id.netWorthButton);
        netWorthButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(AccountManagerActivity.this, NetWorthCalculator.class);
            startActivity(intent);
        });
        Button goalTrackerButton = findViewById(R.id.goalTrackerButton);
        goalTrackerButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(AccountManagerActivity.this, GoalTrackerActivity.class);
            startActivity(intent);
        });
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(AccountManagerActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(v -> {
            savePage();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AccountManagerActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> {
            helpPopup();
        });

        // add the headers to the table
        table = findViewById(R.id.accountsTableLayout);

        // add header row
        TableRow headerRow = new TableRow(this);

        TextView nameHeader = new TextView(this);
        nameHeader.setText(getResources().getString(R.string.accountNameHeader));
        nameHeader.setTextSize(18);
        nameHeader.setTypeface(null, Typeface.BOLD);
        nameHeader.setPadding(5, 5, 10, 5);
        headerRow.addView(nameHeader);

        TextView balanceHeader = new TextView(this);
        balanceHeader.setText(getResources().getString(R.string.accountBalanceHeader));
        balanceHeader.setTextSize(18);
        balanceHeader.setTypeface(null, Typeface.BOLD);
        balanceHeader.setPadding(5, 5, 5, 5);
        headerRow.addView(balanceHeader);

        table.addView(headerRow);

        // populate the table form the saved user data
        populateTableFromSavedData();

        // set up the add new account button
        Button createNewAccountButton = findViewById(R.id.createNewAccountButton);
        createNewAccountButton.setOnClickListener(v -> {
            createNewAccount_Popup();
        });

        // set up the delete account button
        Button deleteAccountButton = findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(v -> {
            deleteAccount_Popup();
        });

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

    // creates a new account within the user object
    private void createNewAccount_Row(String name)
    {
        TableRow tableRow = new TableRow(this);

        // name button
        Button nameButton = new Button(this);
        nameButton.setText(name);
        nameButton.setOnClickListener(v -> editName(v, name));      // handles the popup when the name button is clicked
        tableRow.addView(nameButton);

        // balance button
        Button balanceButton = new Button(this);
        int balanceInCents = GlobalUser.getUser().getAccountBalance(name);
        double balanceInDollars = balanceInCents / 100.0;

        NumberFormat currentFormatter = NumberFormat.getCurrencyInstance(Locale.CANADA);
        String formattedBalance = currentFormatter.format(balanceInDollars);

        balanceButton.setText(formattedBalance);
        balanceButton.setOnClickListener(v -> editBalance(v, name));      // handles the popup when the name button is clicked
        tableRow.addView(balanceButton);

        table.addView(tableRow);
    }

    // creates a popup which allows the user to enter the name and starting balance for a new account
    private void createNewAccount_Popup()
    {
        // test values
        // String name = "";
        // int balance = 0;

        // Inflate the custom layout for the dialog
        LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_new_account, null);
        EditText nameEditText = dialogLayout.findViewById(R.id.editAccountName);
        EditText balanceEditText = dialogLayout.findViewById(R.id.editAccountBalance);

        // create and show the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.name_alertBuilderText))
                .setView(dialogLayout)
                .setPositiveButton("Create New Account", (dialog, which) ->
                {
                    // get user input and update the button text
                    String name = nameEditText.getText().toString().trim();
                    String balance = balanceEditText.getText().toString();
                    Log.d("CreateNewAccountPOPUP", "Creating a new account with input name: " + name + " and input balance: " + balance);
                    int balanceInCents;

                    if(name.isEmpty())
                    {
                        String possibleName = "Account" + (GlobalUser.getUser().getAccountCount() + 1);
                        int n = 2;
                        while(GlobalUser.getUser().accountExists(possibleName))
                        {
                            possibleName = "Account" + (GlobalUser.getUser().getAccountCount() + n);
                            n++;
                        }
                        name = possibleName;
                    }
                    if(balance.isEmpty())
                    {
                        balanceInCents = 0;
                    } else {
                        double balanceInDollars = Double.parseDouble(balance);
                        balanceInCents = (int) (balanceInDollars * 100);
                    }
                    if(!GlobalUser.getUser().accountExists(name))
                    {
                        createNewAccount(name, balanceInCents);
                        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Account with that name already exists.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();


    }

    // creates a popup so that the user can select and delete an account
    private void deleteAccount_Popup()
    {
        // Inflate the custom layout for the dialog
        LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_delete_account, null);
        CheckBox checkBox = dialogLayout.findViewById(R.id.confirmDeleteCheckBox);
        LinearLayout accountsList = dialogLayout.findViewById(R.id.accountsList);
        List<Accounts> accountsToDelete = new ArrayList<>();
        addCheckboxesToPopup(accountsList, accountsToDelete);

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
                deleteCheckedAccounts(accountsToDelete);
                dialog.dismiss();
            }
        });
    }

    // populates the list of accounts which can be used as a liability
    private void addCheckboxesToPopup(LinearLayout container, List<Accounts> accountsToDelete)
    {
        List<Accounts> accounts = GlobalUser.getUser().getAccountsList();
        // get the list of x from the user, add a checkbox for each

        for(int i = 0; i < accounts.size(); i++)
        {
            CheckBox checkBox = new CheckBox(this);
            String displayText = formatCheckBoxName(accounts.get(i));
            checkBox.setText(displayText);
            checkBox.setTag(i);
            checkBox.setOnCheckedChangeListener((v, isChecked) ->
            {
                int position = (int) v.getTag();
                accountsToDelete.add(accounts.get(position));
            });

            container.addView(checkBox);
        }
    }

    // removes the checked accounts from the account list stored in user
    private void deleteCheckedAccounts(List<Accounts> accountsToDelete)
    {
        for(Accounts acc : accountsToDelete)
        {
            GlobalUser.getUser().deleteAccount(acc.getAccountName());
        }
        savePage();
        populateTableFromSavedData();
    }

    // formats the name and balance of the account for the checkbox list
    private String formatCheckBoxName(Accounts acc)
    {
        String msg = "";
        msg+= acc.getAccountName();
        msg+= ": ";

        int balance = acc.getAccountBalance();
        double balanceInDollars = balance / 100.0;
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.CANADA);

        String totalFormatted = currencyFormatter.format(balanceInDollars);

        msg+= totalFormatted;

        return msg;
    }

    // creates a popup which allows the user to enter the name and starting balance for a new account
    private void createNewAccount(String name, int balance)
    {
        GlobalUser.getUser().addAccount(name, balance);

        createNewAccount_Row(name);

        // save data in event of a crash
        savePage();
    }

    // creates a popup which allows the user to edit the account name
    private void editName(View v, String oldName)
    {
        // Inflate the custom layout for the dialog
        LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_edit_account, null);
        TextView currentNameTextView = dialogLayout.findViewById(R.id.currentAccountName);
        EditText editAccountName = dialogLayout.findViewById(R.id.editAccountName);

        // set the current name of the button in the textview
        currentNameTextView.setText(getResources().getString(R.string.currentAccountNameLabel, oldName));

        // cast the view to a button to update the label
        Button button = (Button) v;

        // create and show the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.name_alertBuilderText))
                .setView(dialogLayout)
                .setPositiveButton("Save", (dialog, which) ->
                {
                    // get user input and update the button text
                    String newName = editAccountName.getText().toString().trim();
                    if(!newName.isEmpty())
                    {
                        if(!GlobalUser.getUser().accountExists(newName))
                        {
                            GlobalUser.getUser().modifyAccountName(oldName, newName);
                            button.setText(newName);
                            Toast.makeText(this, "Account name updated!", Toast.LENGTH_SHORT).show();
                            // save data in event of a crash
                            savePage();
                            dialog.dismiss();
                        } else
                        {
                            Toast.makeText(this, "Account with that name already exists. Please try a new name", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Account name cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // creates a popup which allows the user to edit the account balance
    private void editBalance(View view, String name)
    {
        // Inflate the custom layout for the dialog
        LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_edit_balance, null);

        TextView currentBalanceView = dialogLayout.findViewById(R.id.currentBalance);
        EditText editBalanceInput = dialogLayout.findViewById(R.id.editBalance);
        Button cancelButton = dialogLayout.findViewById(R.id.cancelButton);
        Button addButton = dialogLayout.findViewById(R.id.addButton);
        Button subtractButton = dialogLayout.findViewById(R.id.subtractButton);
        Button replaceButton = dialogLayout.findViewById(R.id.replaceButton);

        // Cast the view to a button to get which button was pressed
        Button accountButton = (Button) view;

        // get the current balance of the button from the user
        int balanceInCents = GlobalUser.getUser().getAccountBalance(name);
        double balanceInDollars = balanceInCents / 100.0;
        DecimalFormat currentFormatter = new DecimalFormat("$#,###,##0.00");
        String formattedBalance = currentFormatter.format(balanceInDollars);

        // display the balance in the textview
        currentBalanceView.setText(getResources().getString(R.string.currentAccountBalanceLabel, formattedBalance));

        // Create and show the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.balance_alertBuilderText))
                .setView(dialogLayout)
                .create();

        // button logic
        cancelButton.setOnClickListener(v ->
        {
            dialog.dismiss();
        });

        addButton.setOnClickListener(v ->
        {
            String input = editBalanceInput.getText().toString();
            if(!input.isEmpty())
            {
                // save the updated value
                double inputInDollars = Double.parseDouble(input);
                int inputInCents = (int) (inputInDollars * 100);
                int newBalance = balanceInCents + inputInCents;
                GlobalUser.getUser().modifyAccountBalance(name, newBalance);

                // display the updated value
                updateBalanceOnButton(view, name);
                Toast.makeText(this, "Account balance updated.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        subtractButton.setOnClickListener(v ->
        {
            String input = editBalanceInput.getText().toString();
            if(!input.isEmpty())
            {
                // save the updated value
                double inputInDollars = Double.parseDouble(input);
                int inputInCents = (int) (inputInDollars * 100);
                int newBalance = balanceInCents - inputInCents;
                GlobalUser.getUser().modifyAccountBalance(name, newBalance);

                // display the updated value
                updateBalanceOnButton(view, name);
                Toast.makeText(this, "Account balance updated.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        replaceButton.setOnClickListener(v ->
        {
            String input = editBalanceInput.getText().toString();
            if(!input.isEmpty())
            {
                // save the updated value
                double inputInDollars = Double.parseDouble(input);
                int inputInCents = (int) (inputInDollars * 100);
                GlobalUser.getUser().modifyAccountBalance(name, inputInCents);

                // display the updated value
                updateBalanceOnButton(view, name);
                Toast.makeText(this, "Account balance updated.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        
        dialog.show();

    }

    // updates the button's displayed balance based on the account
    private void updateBalanceOnButton(View v, String name)
    {
        // Cast the view to a button to get which button was pressed
        Button accountButton = (Button) v;

        // get the current balance of the button from the user
        int balanceInCents = GlobalUser.getUser().getAccountBalance(name);
        double balanceInDollars = balanceInCents / 100.0;
        DecimalFormat currentFormatter = new DecimalFormat("$#,###,##0.00");
        String formattedBalance = currentFormatter.format(balanceInDollars);
        accountButton.setText(formattedBalance);

        // save data in event of a crash
        savePage();
    }
    
    // looks through each of the accounts and adds a corresponding row
    private void populateTableFromSavedData()
    {
        table.removeAllViews();
        List<Accounts> accountsList = GlobalUser.getUser().getAccountsList();
        if(accountsList != null && !accountsList.isEmpty())
        {
            for(Accounts acc : accountsList)
            {
                createNewAccount_Row(acc.getAccountName());
            }
            Log.d("populateTable", "Added all accounts to the table");
        } else {
            Log.d("populateTable", "No accounts to add to the list");
        }
    }

    // creates a popup with info on how to navigate the current activity
    private void helpPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.helpPopupTitle));
        builder.setMessage(getResources().getString(R.string.helpPopupInfo_AccountManager));

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