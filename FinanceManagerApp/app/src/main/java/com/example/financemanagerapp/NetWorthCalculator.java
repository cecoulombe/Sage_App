package com.example.financemanagerapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NetWorthCalculator extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    LinearLayout assetAccountList;
    LinearLayout liabilityAccountList;
    TableLayout assetTable;
    TableLayout liabilityTable;
    NumberFormat currencyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        manageDarkMode();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_net_worth_calculator);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.CANADA);
        loadUser(); // should happen in the welcome activity, failsafe

        // set up the buttons across the top to save whatever is currently on the screen and to redirect to the appropriate activity
        // instantiate all buttons and disable the button for the current activity. For the rest, set up the change activity
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(NetWorthCalculator.this, WelcomePageActivity.class);
            startActivity(intent);
        });
        Button accountManagerButton = findViewById(R.id.accountManagerButton);
        accountManagerButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(NetWorthCalculator.this, AccountManagerActivity.class);
            startActivity(intent);
        });
        Button netWorthButton = findViewById(R.id.netWorthButton);
        netWorthButton.setEnabled(false);
        Button goalTrackerButton = findViewById(R.id.goalTrackerButton);
        goalTrackerButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(NetWorthCalculator.this, GoalTrackerActivity.class);
            startActivity(intent);
        });
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            savePage();
            Intent intent = new Intent(NetWorthCalculator.this, SettingsActivity.class);
            startActivity(intent);
        });
        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(v -> {
            savePage();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(NetWorthCalculator.this, MainActivity.class);
            startActivity(intent);
        });

        // create the button to show info for the page
        ImageButton helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> {
            helpPopup();
        });

        // create the button to show the graph
        ImageButton showGraphButton = findViewById(R.id.showGraphButton);
        showGraphButton.setOnClickListener(v -> {
            graphPopup();
        });

        // set up the delete account button
        Button deleteAssetButton = findViewById(R.id.deleteAssetButton);
        deleteAssetButton.setOnClickListener(v -> {
            deleteAccount_Popup("asset");
        });

        Button deleteLiabilityButton = findViewById(R.id.deleteLiabilityButton);
        deleteLiabilityButton.setOnClickListener(v -> {
            deleteAccount_Popup("liability");
        });


        // allow nested scrolling
        ScrollView assetAccountsScrollView = findViewById(R.id.assetAccountsScrollView);
        assetAccountsScrollView.setNestedScrollingEnabled(true);

        ScrollView assetsScrollView = findViewById(R.id.assetsScrollView);
        assetsScrollView.setNestedScrollingEnabled(true);

        ScrollView liabilityAccountsScrollView = findViewById(R.id.liabilityAccountsScrollView);
        liabilityAccountsScrollView.setNestedScrollingEnabled(true);

        ScrollView liabilitiesScrollView = findViewById(R.id.liabilitiesScrollView);
        liabilitiesScrollView.setNestedScrollingEnabled(true);
        
        // asset account list
        assetAccountList = findViewById(R.id.assetAccountsList);
        addCheckBoxesToAssets(assetAccountList);

        // asset table
        assetTable = findViewById(R.id.assetsTableLayout);
        setUpTable(assetTable, "asset");

        // add asset button
        Button addAssetButton = findViewById(R.id.addAssetButton);
        addAssetButton.setOnClickListener(v -> {
            createNewAccount_Popup(assetTable, "asset");
        });

        // liability account list
        liabilityAccountList = findViewById(R.id.liabilityAccountsList);
        addCheckBoxesToLiabilities(liabilityAccountList);

        // liability table
        liabilityTable = findViewById(R.id.liabilitiesTableLayout);
        setUpTable(liabilityTable, "liability");

        // add liability button
        Button addLiabilityButton = findViewById(R.id.addLiabilityButton);
        addLiabilityButton.setOnClickListener(v -> {
            createNewAccount_Popup(liabilityTable, "liability");
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

    // populates the list of accounts which can be used as an asset
    private void addCheckBoxesToAssets(LinearLayout container)
    {
        List<Accounts> accounts = GlobalUser.getUser().getAccountsList();
        // get the list of x from the user, add a checkbox for each

        for(int i = 0; i < accounts.size(); i++)
        {
            CheckBox checkBox = new CheckBox(this);
            String displayText = formatCheckBoxName(accounts.get(i));
            checkBox.setText(displayText);
            checkBox.setTag(i);
            if(GlobalUser.getUser().getIsAsset(accounts.get(i).getAccountName()))
            {
                checkBox.setChecked(true);
            } else if(GlobalUser.getUser().getIsLiability(accounts.get(i).getAccountName()))    // if asset, disable it
            {
                checkBox.setEnabled(false);
            }
            checkBox.setOnCheckedChangeListener((v, isChecked) ->
            {
                int position = (int) v.getTag();
                GlobalUser.getUser().setIsAsset(accounts.get(position).getAccountName(), isChecked);
                savePage();

                updateCheckBoxState(liabilityAccountList, accounts.get(position).getAccountName(), !isChecked, false);

            });

            container.addView(checkBox);
        }
    }

    // populates the list of accounts which can be used as a liability
    private void addCheckBoxesToLiabilities(LinearLayout container)
    {
        List<Accounts> accounts = GlobalUser.getUser().getAccountsList();
        // get the list of x from the user, add a checkbox for each

        for(int i = 0; i < accounts.size(); i++)
        {
            CheckBox checkBox = new CheckBox(this);
            String displayText = formatCheckBoxName(accounts.get(i));
            checkBox.setText(displayText);
            checkBox.setTag(i);
            if(GlobalUser.getUser().getIsLiability(accounts.get(i).getAccountName()))
            {
                checkBox.setChecked(true);
            } else if(GlobalUser.getUser().getIsAsset(accounts.get(i).getAccountName()))    // if asset, disable it
            {
                checkBox.setEnabled(false);
            }
            checkBox.setOnCheckedChangeListener((v, isChecked) ->
            {
                int position = (int) v.getTag();
                GlobalUser.getUser().setIsLiability(accounts.get(position).getAccountName(), isChecked);
                savePage();

                updateCheckBoxState(assetAccountList, accounts.get(position).getAccountName(), !isChecked, false);

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


    // disables or enables a button in the opposite view depending on what action was taken
    private void updateCheckBoxState(LinearLayout container, String name, boolean isEnabled, boolean isChecked)
    {
        List<Accounts> accounts = GlobalUser.getUser().getAccountsList();
        // iterate through the container to find the corresponding view
        for(int i = 0; i < container.getChildCount(); i++)
        {
            View child = container.getChildAt(i);
            if(child instanceof CheckBox)
            {
                CheckBox checkBox = (CheckBox) child;
                
                // match the checkbox by its text

                int position = (int) checkBox.getTag();

                if(accounts.get(position).getAccountName().equals(name))
                {
                    checkBox.setEnabled(isEnabled);
                    checkBox.setChecked(isChecked);
                    return;
                }
            }
        }
    }

    // creates the table headers
    private void setUpTable(TableLayout table, String type)
    {
        // add header row
        TableRow headerRow = new TableRow(this);

        TextView nameHeader = new TextView(this);
        nameHeader.setText(getResources().getString(R.string.nameHeader));
        nameHeader.setTextSize(18);
        nameHeader.setTypeface(null, Typeface.BOLD);
        nameHeader.setPadding(5, 5, 10, 5);
        headerRow.addView(nameHeader);

        TextView balanceHeader = new TextView(this);
        balanceHeader.setText(getResources().getString(R.string.balanceHeader));
        balanceHeader.setTextSize(18);
        balanceHeader.setTypeface(null, Typeface.BOLD);
        balanceHeader.setPadding(5, 5, 5, 5);
        headerRow.addView(balanceHeader);

        table.addView(headerRow);

        // populate the table form the saved user data
        populateTableFromSavedData(table, type);

    }

    // populates the table with the existing data
    // looks through each of the accounts and adds a corresponding row
    private void populateTableFromSavedData(TableLayout table, String type)
    {
        table.removeAllViews();
        List<Accounts> accountsList;
        if(type.equals("asset"))
        {
            accountsList = GlobalUser.getUser().getAssetsList();
        } else {
            accountsList = GlobalUser.getUser().getLiabilitiesList();
        }

        if(accountsList != null && !accountsList.isEmpty())
        {
            for(Accounts acc : accountsList)
            {
                createNewAccount_Row(table, type, acc.getAccountName());
            }
            Log.d("populateTable", "Added all accounts to the table");
        } else {
            Log.d("populateTable", "No accounts to add to the list");
        }
    }

    // creates a new account within the user object
    private void createNewAccount_Row(TableLayout table, String type, String name)
    {
        TableRow tableRow = new TableRow(this);

        // name button
        Button nameButton = new Button(this);
        nameButton.setText(name);
        nameButton.setOnClickListener(v -> editName(v, name));      // handles the popup when the name button is clicked
        tableRow.addView(nameButton);

        // balance button
        Button balanceButton = new Button(this);
        int balanceInCents = type.equals("asset") ? GlobalUser.getUser().getAssetBalance(name) : GlobalUser.getUser().getLiabilityBalance(name);
        double balanceInDollars = balanceInCents / 100.0;

        String formattedBalance = currencyFormatter.format(balanceInDollars);

        balanceButton.setText(formattedBalance);
        balanceButton.setOnClickListener(v -> editBalance(v, type, name));      // handles the popup when the name button is clicked
        tableRow.addView(balanceButton);

        table.addView(tableRow);
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
    private void editBalance(View view, String type,  String name)
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
        int balanceInCents = type.equals("asset") ? GlobalUser.getUser().getAssetBalance(name) : GlobalUser.getUser().getLiabilityBalance(name);
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
                if(type.equals("asset"))
                {
                    GlobalUser.getUser().modifyAssetBalance(name, newBalance);
                } else {
                    GlobalUser.getUser().modifyLiabilityBalance(name, newBalance);
                }

                // display the updated value
                updateBalanceOnButton(view, type, name);
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
                if(type.equals("asset"))
                {
                    GlobalUser.getUser().modifyAssetBalance(name, newBalance);
                } else {
                    GlobalUser.getUser().modifyLiabilityBalance(name, newBalance);
                }
                // display the updated value
                updateBalanceOnButton(view, type, name);
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
                if(type.equals("asset"))
                {
                    GlobalUser.getUser().modifyAssetBalance(name, inputInCents);
                } else {
                    GlobalUser.getUser().modifyLiabilityBalance(name, inputInCents);
                }
                // display the updated value
                updateBalanceOnButton(view, type, name);
                Toast.makeText(this, "Account balance updated.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // updates the button's displayed balance based on the account
    private void updateBalanceOnButton(View v, String type, String name)
    {
        // Cast the view to a button to get which button was pressed
        Button accountButton = (Button) v;

        // get the current balance of the button from the user
        int balanceInCents = type.equals("asset") ? GlobalUser.getUser().getAssetBalance(name) : GlobalUser.getUser().getLiabilityBalance(name);
        double balanceInDollars = balanceInCents / 100.0;
        DecimalFormat currentFormatter = new DecimalFormat("$#,###,##0.00");
        String formattedBalance = currentFormatter.format(balanceInDollars);

        accountButton.setText(formattedBalance);

        // save data in event of a crash
        savePage();
    }

    // creates a popup which allows the user to enter the name and starting balance for a new account
    private void createNewAccount_Popup(TableLayout table, String type)
    {
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
                        String possibleName = type.equals("asset") ? "Asset" + (GlobalUser.getUser().getAssetCount() + 1) : "Liability" + (GlobalUser.getUser().getLiabilityCount() + 1);
                        int n = 2;
                        while(GlobalUser.getUser().accountExists(possibleName))
                        {
                            possibleName = type.equals("asset") ? "Asset" + (GlobalUser.getUser().getAssetCount() + 1) : "Liability" + (GlobalUser.getUser().getLiabilityCount() + n);
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
                    if(type.equals("asset"))
                    {
                        if(!GlobalUser.getUser().assetExists(name))
                        {
                            createNewAccount(table, type, name, balanceInCents);
                            Toast.makeText(this, "Asset created!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Asset with that name already exists.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if(!GlobalUser.getUser().liabilityExists(name))
                        {
                            createNewAccount(table, type, name, balanceInCents);
                            Toast.makeText(this, "Liability created!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Liability with that name already exists.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();


    }

    // creates a popup which allows the user to enter the name and starting balance for a new account
    private void createNewAccount(TableLayout table, String type, String name, int balance)
    {
        if(type.equals("asset"))
        {
            GlobalUser.getUser().addAsset(name, balance);
        } else {
            GlobalUser.getUser().addLiability(name, balance);
        }

        createNewAccount_Row(table, type, name);

        // save data in event of a crash
        savePage();
    }

    // creates a popup so that the user can select and delete an account
    private void deleteAccount_Popup(String type)
    {
        // Inflate the custom layout for the dialog
        LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_delete_account, null);
        CheckBox checkBox = dialogLayout.findViewById(R.id.confirmDeleteCheckBox);
        LinearLayout accountsList = dialogLayout.findViewById(R.id.accountsList);
        List<Accounts> accountsToDelete = new ArrayList<>();
        addCheckboxesToPopup(type, accountsList, accountsToDelete);

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
                deleteCheckedAccounts(type, accountsToDelete);
                dialog.dismiss();
            }
        });
    }

    // populates the list of accounts which can be used as a liability
    private void addCheckboxesToPopup(String type, LinearLayout container, List<Accounts> accountsToDelete)
    {
        List<Accounts> accounts = type.equals("asset") ? GlobalUser.getUser().getAssetsList() : GlobalUser.getUser().getLiabilitiesList();
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
    private void deleteCheckedAccounts(String type, List<Accounts> accountsToDelete)
    {
        for(Accounts acc : accountsToDelete)
        {
            if (type.equals("asset"))
            {
                GlobalUser.getUser().deleteAsset(acc.getAccountName());
            } else {
                GlobalUser.getUser().deleteLiability(acc.getAccountName());
            }
        }
        savePage();

        TableLayout table;
        if(type.equals("asset"))
        {
            table = assetTable;
        } else {
            table = liabilityTable;
        }

        populateTableFromSavedData(table, type);
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

    // calculates the net worth and updates the display
    private void updateNetWorth()
    {
        int sum = getAssetAccounts() + getAssetSum() - getLiabilityAccounts() - getLiabilitySum();
        double netWorth = sum / 100.0;

        String formattedBalance = currencyFormatter.format(netWorth);

        TextView netWorthTextView = findViewById(R.id.netWorthDisplayTextView);
        netWorthTextView.setText(getResources().getString(R.string.netWorthDisplay, formattedBalance));
        if (sum > 0) {
            netWorthTextView.setTextColor(getResources().getColor(R.color.netPositive));
        } else if (sum < 0) {
            netWorthTextView.setTextColor(getResources().getColor(R.color.netNegative));
        } else {
            netWorthTextView.setTextColor(getResources().getColor(R.color.textColor));
        }

        String date = getDate();
        GlobalUser.getUser().addNetWorth(sum, date);
    }

    // returns the current date as a string
    private String getDate()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }


    // returns the sum of all asset accounts
    private int getAssetAccounts()
    {
        int sum = 0;
        List<Accounts> accounts = GlobalUser.getUser().getAccountsList();

        if(accounts == null)
        {
            return 0;
        }

        for(Accounts acc : accounts)
        {
            if(acc.getIsAsset())
            {
                sum += acc.getAccountBalance();
            }
        }

        return sum;
    }

    // returns the sum of all assets
    private int getAssetSum()
    {
        int sum = 0;
        List<Accounts> assets = GlobalUser.getUser().getAssetsList();

        if(assets == null)
        {
            return 0;
        }

        for(Accounts ass : assets)
        {
            sum += ass.getAccountBalance();
        }

        return sum;
    }

    // returns the sum of all liability accounts
    private int getLiabilityAccounts()
    {
        int sum = 0;
        List<Accounts> accounts = GlobalUser.getUser().getAccountsList();

        if(accounts == null)
        {
            return 0;
        }

        for(Accounts acc : accounts)
        {
            if(acc.getIsLiability())
            {
                sum += acc.getAccountBalance();
            }
        }

        return sum;
    }

    // returns the sum of all liabilities
    private int getLiabilitySum()
    {
        int sum = 0;
        List<Accounts> liabilities = GlobalUser.getUser().getLiabilitiesList();

        if(liabilities == null)
        {
            return 0;
        }

        for(Accounts liab : liabilities)
        {
            sum += liab.getAccountBalance();
        }

        return sum;
    }

    // creates and displays the popup with the chart for net worth history
    private void graphPopup()
    {
        // show the populated graph
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_graph);
        dialog.setCancelable(true);

        GraphView graphView = dialog.findViewById(R.id.graphView);

        populateGraphView(graphView);

        // Find and set up the close button (X) in the top-right corner
        ImageButton closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dialog.dismiss()); // Dismiss the dialog when the button is clicked

        dialog.show();
    }

    // populates the graph with the net worth history data
    private void populateGraphView(GraphView graphView)
    {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        List<NetWorth> netWorthList = GlobalUser.getUser().getNetWorthHistory();

        if(netWorthList != null && !netWorthList.isEmpty())
        {
            // sort the list chronologically
            Collections.sort(netWorthList, new Comparator<NetWorth>() {
                @Override
                public int compare(NetWorth o1, NetWorth o2)
                {
                    return parseDate(o1.getDate()).compareTo(parseDate(o2.getDate()));
                }
            });

            // prepare DataPoints for the graph
            int n = netWorthList.size();
            DataPoint[] dataPoints = new DataPoint[n];
            String[] dateLabels = new String[n];
            for(int i = 0; i < n; i++)
            {
                NetWorth netWorth = netWorthList.get(i);
                Date date = parseDate(netWorth.getDate());
                int balanceInCents = netWorth.getNetWorth();
                double balanceInDollars = balanceInCents / 100.0;

                dataPoints[i] = new DataPoint(date.getTime(), balanceInDollars);

                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
                dateLabels[i] = sdf.format(date);
            }

            // update the graph series
            series.resetData(dataPoints);

            series.setThickness(8);
            series.setColor(getResources().getColor(R.color.textColor));

            // add the series to the graph
            graphView.addSeries(series);

            // customize the graph
            customizeGraph(graphView, dateLabels, series);
        }
    }

    // converts the date string into a usable date
    private Date parseDate(String dateString)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        try
        {
            return sdf.parse(dateString);
        } catch (ParseException e)
        {
            e.printStackTrace();
            return new Date(0); // fall back to epoch if parsing fails
        }
    }

    // customizes the graph
    private void customizeGraph(GraphView graphView, String[] dateLabels, LineGraphSeries<DataPoint> series)
    {
        graphView.setTitle(getResources().getString(R.string.netWorthGraphTitle));
        graphView.getGridLabelRenderer().setHorizontalAxisTitle(getResources().getString(R.string.xAxisLabel));
        graphView.getGridLabelRenderer().setVerticalAxisTitle(getResources().getString(R.string.yAxisLabel));

        // Disable horizontal axis labels (x-axis)
        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return ""; // Return an empty string for x-axis labels
                } else {
                    return super.formatLabel(value, isValueX); // Keep y-axis labels as is
                }
            }
        });

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(series.getLowestValueX());
        graphView.getViewport().setMaxX(series.getHighestValueX());

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(series.getLowestValueY());
        graphView.getViewport().setMaxY(series.getHighestValueY());

        // enable scrolling and zooming
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScalable(true);

    }

    // saves the user data to firebase asynchronously (.set method is async by nature)
    private void savePage()
    {
        updateNetWorth();
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

        updateNetWorth();

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