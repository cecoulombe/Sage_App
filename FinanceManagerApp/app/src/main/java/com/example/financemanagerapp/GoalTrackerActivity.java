package com.example.financemanagerapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoalTrackerActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    Button goalNameButton;
    Button goalAmountButton;
    Button currentAmountButton;
    NumberFormat currencyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        manageDarkMode();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_goal_tracker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth and everything else
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.CANADA);
        goalNameButton = findViewById(R.id.goalNameButton);
        goalAmountButton = findViewById(R.id.goalAmountButton);
        currentAmountButton = findViewById(R.id.currentAmountButton);


        loadUser(); // should happen in the welcome activity, failsafe
        if(GlobalUser.getUser().getGoalName().equals(""))
        {
            createGoal_Popup();
        } else {
            setUpChart();
        }


        // initialize and implement the buttons that allow the user to edit the goal name, balance, and target amount
        goalNameButton.setText(GlobalUser.getUser().getGoalName());
        goalNameButton.setOnClickListener(v -> {
            editGoalName_Popup(v, goalNameButton.getText().toString());
            savePage();
        });

        int goalAmountInCents = GlobalUser.getUser().getGoalAmount();
        double goalAmountInDollars = goalAmountInCents / 100.0;
        String formattedGoal = currencyFormatter.format(goalAmountInDollars);
        goalAmountButton.setText(formattedGoal);

        goalAmountButton.setOnClickListener(v -> {
            editGoalTarget_Popup(v);
            savePage();
        });

        int currentInCents = GlobalUser.getUser().getCurrentAmount();
        updateCurrentAmountButton();
        currentAmountButton.setOnClickListener(v -> {
            editCurrentAmount_Popup(v);
            savePage();
        });

        // set up the buttons across the top to save whatever is currently on the screen and to redirect to the appropriate activity
        // instantiate all buttons and disable the button for the current activity. For the rest, set up the change activity
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(GoalTrackerActivity.this, WelcomePageActivity.class);
            startActivity(intent);
        });
        Button accountManagerButton = findViewById(R.id.accountManagerButton);
        accountManagerButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(GoalTrackerActivity.this, AccountManagerActivity.class);
            startActivity(intent);
        });
        Button netWorthButton = findViewById(R.id.netWorthButton);
        netWorthButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(GoalTrackerActivity.this, NetWorthCalculator.class);
            startActivity(intent);
        });

        Button goalTrackerButton = findViewById(R.id.goalTrackerButton);
        goalTrackerButton.setEnabled(false);

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(GoalTrackerActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(v -> {
            savePage();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(GoalTrackerActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> {
            helpPopup();
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

    // populates the pie chart to display current goal progress
    private void setUpChart()
    {
        PieChart pieChart = findViewById(R.id.pieChart);

        // Retrieve amounts
        int currentAmount = getTotalAmount();
        int goalAmount = GlobalUser.getUser().getGoalAmount();

        // Convert amounts to dollars with precision
        BigDecimal currentInDollars = new BigDecimal(currentAmount).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        BigDecimal goalInDollars = new BigDecimal(goalAmount).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        BigDecimal remainingDollars = goalInDollars.subtract(currentInDollars);

        // Calculate percentage with precision
        BigDecimal currentPercent = new BigDecimal(currentAmount)
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(goalAmount), 2, RoundingMode.HALF_UP);

        // Populate the data
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(currentInDollars.floatValue(), "Saved $"));
        entries.add(new PieEntry(remainingDollars.floatValue(), "Remaining $"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                getResources().getColor(R.color.tableLayoutBackground),
                getResources().getColor(R.color.headerBackground)
        });
        dataSet.setValueTextColor(getResources().getColor(R.color.textAccentColor));
        dataSet.setValueTextSize(20f);

        // Set a custom value formatter for two decimal places
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("$%.2f", value); // Ensures 2 decimal precision
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Make it a donut chart
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleColor(getResources().getColor(R.color.backgroundColor));

        // Customize chart appearance
        pieChart.setDrawEntryLabels(false); // Hide labels outside the pie
        pieChart.getDescription().setEnabled(false); // Remove description label
        pieChart.setCenterText(String.format("%.2f%%", currentPercent.floatValue())); // Add center text
        pieChart.setCenterTextSize(30f);
        pieChart.setCenterTextColor(getResources().getColor(R.color.textColor));
        Legend legend = pieChart.getLegend();
        legend.setTextSize(16f); // Increase legend text size
        legend.setTextColor(getResources().getColor(R.color.textColor)); // Optionally set legend text color
        legend.setXEntrySpace(10f); // Add spacing between legend items
        legend.setYEntrySpace(10f); // Add vertical spacing

        // Refresh the chart
        pieChart.invalidate();
    }

    // updates the pie chart to accurately reflect goal progress
    private void updatePieChart() {
        PieChart pieChart = findViewById(R.id.pieChart);

        // Retrieve amounts
        int currentAmount = getTotalAmount();
        int goalAmount = GlobalUser.getUser().getGoalAmount();

        // Convert amounts to dollars with precision
        BigDecimal currentInDollars = new BigDecimal(currentAmount).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        BigDecimal goalInDollars = new BigDecimal(goalAmount).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        BigDecimal remainingDollars = goalInDollars.subtract(currentInDollars);

        // Calculate percentage with precision
        BigDecimal currentPercent = new BigDecimal(currentAmount)
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(goalAmount), 2, RoundingMode.HALF_UP);

        // Populate the data
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(currentInDollars.floatValue(), "Saved $"));
        entries.add(new PieEntry(remainingDollars.floatValue(), "Remaining $"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                getResources().getColor(R.color.tableLayoutBackground),
                getResources().getColor(R.color.headerBackground)
        });
        dataSet.setValueTextColor(getResources().getColor(R.color.textAccentColor));
        dataSet.setValueTextSize(20f);

        // Set a custom value formatter for two decimal places
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("$%.2f", value); // Ensures 2 decimal precision
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Make it a donut chart
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleColor(getResources().getColor(R.color.backgroundColor));

        // Customize chart appearance
        pieChart.setDrawEntryLabels(false); // Hide labels outside the pie
        pieChart.getDescription().setEnabled(false); // Remove description label
        pieChart.setCenterText(String.format("%.2f%%", currentPercent.floatValue())); // Add center text
        pieChart.setCenterTextSize(30f);
        pieChart.setCenterTextColor(getResources().getColor(R.color.textColor));
        Legend legend = pieChart.getLegend();
        legend.setTextSize(16f); // Increase legend text size
        legend.setTextColor(getResources().getColor(R.color.textColor)); // Optionally set legend text color
        legend.setXEntrySpace(10f); // Add spacing between legend items
        legend.setYEntrySpace(10f); // Add vertical spacing

        // Refresh the chart
        pieChart.invalidate();
    }

    // creates a popup which prompts the user to create a new financial goal
    private void createGoal_Popup()
    {
        // Inflate the custom layout for the dialog
        ConstraintLayout dialogLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_create_goal, null);
        EditText nameEditText = dialogLayout.findViewById(R.id.enterNameEditText);
        EditText balanceEditText = dialogLayout.findViewById(R.id.enterAmountEditText);
        LinearLayout accountsList = dialogLayout.findViewById(R.id.accountsList);
        addCheckboxesToPopup(accountsList);


        // create and show the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.goalPopupTitle))
                .setView(dialogLayout)
                .setPositiveButton("Create Goal", (dialog, which) ->
                {
                    // get user input and update the button text
                    String name = nameEditText.getText().toString().trim();
                    String balance = balanceEditText.getText().toString();
                    Log.d("CreateNewAccountPOPUP", "Creating a new goal with input name: " + name + " and input balance: " + balance);
                    int balanceInCents;

                    if(!name.isEmpty() && !balance.isEmpty())
                    {
                        double balanceInDollars = Double.parseDouble(balance);
                        balanceInCents = (int) (balanceInDollars * 100);
                        GlobalUser.getUser().setGoalName(name);
                        GlobalUser.getUser().setGoalAmount(balanceInCents);
                        savePage();
                        setUpChart();
                        goalNameButton.setText(GlobalUser.getUser().getGoalName());
                        updateCurrentAmountButton();
                        int goalAmountInCents = GlobalUser.getUser().getGoalAmount();
                        double goalAmountInDollars = goalAmountInCents / 100.0;
                        String formattedGoal = currencyFormatter.format(goalAmountInDollars);
                        goalAmountButton.setText(formattedGoal);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Goal must have a name and target amount!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();


    }


    // populates the list of accounts which can be used as a liability
    private void addCheckboxesToPopup(LinearLayout container)
    {
        List<Accounts> accounts = GlobalUser.getUser().getAccountsList();
        // get the list of x from the user, add a checkbox for each

        for(int i = 0; i < accounts.size(); i++)
        {
            CheckBox checkBox = new CheckBox(this);
            String displayText = formatCheckBoxName(accounts.get(i));
            checkBox.setText(displayText);
            checkBox.setTag(i);
            if(GlobalUser.getUser().getGoalFlag(accounts.get(i).getAccountName()))
            {
                checkBox.setChecked(true);
            }
            checkBox.setOnCheckedChangeListener((v, isChecked) ->
            {
                int position = (int) v.getTag();
                GlobalUser.getUser().setGoalFlag(accounts.get(position).getAccountName(), isChecked);
                savePage();
            });

            container.addView(checkBox);
        }
    }


    // formats the name and balance of the account for the checkbox list
    private String formatCheckBoxName(Accounts acc)
    {
        String msg = "";
        msg+= acc.getAccountName();
        msg+= ": ";

        int balance = acc.getAccountBalance();
        double balanceInDollars = balance / 100.0;
        String totalFormatted = currencyFormatter.format(balanceInDollars);

        msg+= totalFormatted;

        return msg;
    }

    // creates a popup which allows the user to edit the account name
    private void editGoalName_Popup(View v, String oldName)
    {
        // Inflate the custom layout for the dialog
        LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_edit_goal_name, null);
        TextView currentNameTextView = dialogLayout.findViewById(R.id.currentGoalName);
        EditText editGoalName = dialogLayout.findViewById(R.id.editGoalName);

        // set the current name of the button in the textview
        currentNameTextView.setText(getResources().getString(R.string.currentGoalNameLabel, oldName));

        // cast the view to a button to update the label
        Button button = (Button) v;

        // create and show the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.editGoalName_title))
                .setView(dialogLayout)
                .setPositiveButton("Save", (dialog, which) ->
                {
                    // get user input and update the button text
                    String newName = editGoalName.getText().toString().trim();
                    if(!newName.isEmpty())
                    {
                        button.setText(newName);
                        GlobalUser.getUser().setGoalName(newName);
                        Toast.makeText(this, "Account name updated!", Toast.LENGTH_SHORT).show();
                        // save data in event of a crash
                        savePage();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Goal name cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // creates a popup which allows the user to edit the account name
    private void editGoalTarget_Popup(View v)
    {
        // Inflate the custom layout for the dialog
        LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_edit_goal_amount, null);
        TextView currentAmountTextView = dialogLayout.findViewById(R.id.currentAmountLabel);
        EditText newAmount = dialogLayout.findViewById(R.id.newAmountEdit);

        // set the current name of the button in the textview
        int currentTarget = GlobalUser.getUser().getGoalAmount();
        double currentInDollars = currentTarget / 100.0;
        String formattedCurrent = currencyFormatter.format(currentInDollars);
        currentAmountTextView.setText(getResources().getString(R.string.currentGoalAmountLabel, formattedCurrent));

        // cast the view to a button to update the label
        Button button = (Button) v;

        // create and show the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.editGoalAmount_title))
                .setView(dialogLayout)
                .setPositiveButton("Save", (dialog, which) ->
                {
                    // get user input and update the button text
                    String newTarget = newAmount.getText().toString().trim();
                    if(!newTarget.isEmpty())
                    {
                        double inputInDollars = Double.parseDouble(newTarget);
                        int inputInCents = (int) (inputInDollars * 100);
                        GlobalUser.getUser().setGoalAmount(inputInCents);

                        String newTargetFormatted = currencyFormatter.format(inputInDollars);
                        button.setText(newTargetFormatted);

                        Toast.makeText(this, "Goal target amount updated!", Toast.LENGTH_SHORT).show();
                        // save data in event of a crash
                        updatePieChart();
                        savePage();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Goal target cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // creates a popup which allows the user to edit the account balance
    private void editCurrentAmount_Popup(View view)
    {
        // Inflate the custom layout for the dialog
        LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_edit_current_goal_amount, null);

        TextView currentBalanceView = dialogLayout.findViewById(R.id.currentBalance);
        EditText editBalanceInput = dialogLayout.findViewById(R.id.editBalance);
        Button cancelButton = dialogLayout.findViewById(R.id.cancelButton);
        Button addButton = dialogLayout.findViewById(R.id.addButton);
        Button subtractButton = dialogLayout.findViewById(R.id.subtractButton);
        Button replaceButton = dialogLayout.findViewById(R.id.replaceButton);

        LinearLayout accountsList_EditCurrent = dialogLayout.findViewById(R.id.accountsList);
        addCheckboxesToPopup(accountsList_EditCurrent);

        // Cast the view to a button to get which button was pressed
        Button button = (Button) view;

        int currentSaved = GlobalUser.getUser().getCurrentAmount();
        double currentInDollars = currentSaved / 100.0;
        String currentFormatted = currencyFormatter.format(currentInDollars);

        // display the balance in the textview
        int accountsTotal = getAmountFromAccounts();
        double accountsInDollars = accountsTotal / 100.0;
        String accountsFormatted = currencyFormatter.format(accountsInDollars);

        int total = getTotalAmount();
        double totalInDollars = total / 100.0;
        String totalFormatted = currencyFormatter.format(totalInDollars);
        currentBalanceView.setText(getResources().getString(R.string.currentAmountSavedLabel, currentFormatted, accountsFormatted, totalFormatted));

        // Create and show the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.balance_alertBuilderText))
                .setView(dialogLayout)
                .create();

        // button logic
        cancelButton.setOnClickListener(v ->
        {
            savePage();
            updateCurrentAmountButton();
            updatePieChart();
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
                int newBalance = currentSaved + inputInCents;
                GlobalUser.getUser().setCurrentAmount(newBalance);

                // display the updated value
                savePage();
                updateCurrentAmountButton();
                updatePieChart();
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
                int newBalance = currentSaved - inputInCents;
                GlobalUser.getUser().setCurrentAmount(newBalance);

                // display the updated value
                savePage();
                updateCurrentAmountButton();
                updatePieChart();
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
                GlobalUser.getUser().setCurrentAmount(inputInCents);

                // display the updated value
                savePage();
                updateCurrentAmountButton();
                updatePieChart();
                Toast.makeText(this, "Account balance updated.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    // accurately updates the current amount displayed on the button
    private void updateCurrentAmountButton()
    {
        int total = getTotalAmount();
        double totalInDollars = total / 100.0;
        String totalFormatted = currencyFormatter.format(totalInDollars);
        currentAmountButton.setText(totalFormatted);
    }

    // returns the sum of all accounts contributing to the goal
    private int getAmountFromAccounts()
    {
        int sum = 0;
        List<Accounts> accounts = GlobalUser.getUser().getAccountsList();

        if(accounts == null)
        {
            return 0;
        }

        for(Accounts acc : accounts)
        {
            if(acc.getUseInGoal())
            {
                sum += acc.getAccountBalance();
            }
        }

        return sum;
    }

    // returns the total amount of money towards the goal
    private int getTotalAmount()
    {
        int currentSaved = GlobalUser.getUser().getCurrentAmount();

        int accountsTotal = getAmountFromAccounts();

        int total = currentSaved + accountsTotal;

        return total;
    }


    // creates a popup with info on how to navigate the current activity
    private void helpPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.helpPopupTitle));
        builder.setMessage(getResources().getString(R.string.helpPopupInfo_GoalTracker));

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