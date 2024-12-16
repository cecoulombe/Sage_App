package com.example.financemanagerapp;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class User {

    // attributes
    private String name;
    private List<Accounts> accountsList;
    private List<Accounts> assetsList;
    private List<Accounts> liabilitiesList;
    private List<NetWorth> netWorthHistory;
    private int goalAmount;
    private int currentAmount;
    private String goalName;

    // public constructor for firebase
    public User() {}

    // constructor to create a new user
    public User(String name)
    {
        this.name = name;
        accountsList = new ArrayList<>();
        assetsList = new ArrayList<>();
        liabilitiesList = new ArrayList<>();
        netWorthHistory = new ArrayList<>();
        addFirstNetWorth();
        goalAmount = 0;
        goalName = "";
        currentAmount = 0;
    }

    // returns the user's name
    public String getName()
    {
        return name;
    }

    // returns the list of accounts
    public List<Accounts> getAccountsList()
    {
        return accountsList;
    }

    // returns the list of assets
    public List<Accounts> getAssetsList()
    {
        return assetsList;
    }

    // returns the list of liabilities
    public List<Accounts> getLiabilitiesList()
    {
        return liabilitiesList;
    }

    // returns the list of liabilities
    public List<NetWorth> getNetWorthHistory()
    {
        return netWorthHistory;
    }

    // returns the goal name
    public String getGoalName()
    {
        return goalName;
    }

    // returns the goal amount
    public int getGoalAmount()
    {
        return goalAmount;
    }

    // returns the current amount saved towards the goal
    public int getCurrentAmount()
    {
        return currentAmount;
    }


    // sets the user's name
    public void setName(String newName)
    {
        name = newName;
    }

    // adds an account to the list
    public void addAccount(String accountName, int accountBalance)
    {
        if(!accountExists(accountName))
        {
            accountsList.add(new Accounts(accountName, accountBalance));
        }
        Log.d("addAccount", "An account with that name already exists");
    }

    // modifies an account name
    public void modifyAccountName(String oldName, String newName)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(oldName))
            {
                acc.setAccountName(newName);
                return;
            }
        }
        Log.d("modifyAccountName", "No such account exists");
    }

    // modifies an account balance
    public void modifyAccountBalance(String name, int newBalance)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                acc.setAccountBalance(newBalance);
                return;
            }
        }
        Log.d("modifyAccountBalance", "No such account exists");
    }

    // returns a specific account reference
    public int getAccountBalance(String name)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                return acc.getAccountBalance();
            }
        }
        Log.d("modifyAccountBalance", "No such account exists");
        return 0;
    }

    // adds an account to the list
    public void addAsset(String accountName, int accountBalance)
    {
        if(!assetExists(accountName))
        {
            assetsList.add(new Accounts(accountName, accountBalance));
        }
        Log.d("addAccount", "An account with that name already exists");
    }

    // modifies an account name
    public void modifyAssetName(String oldName, String newName)
    {
        for(Accounts acc : assetsList)
        {
            if(acc.getAccountName().equals(oldName))
            {
                acc.setAccountName(newName);
                return;
            }
        }
        Log.d("modifyAccountName", "No such account exists");
    }

    // modifies an account balance
    public void modifyAssetBalance(String name, int newBalance)
    {
        for(Accounts acc : assetsList)
        {
            if(acc.getAccountName().equals(name))
            {
                acc.setAccountBalance(newBalance);
                return;
            }
        }
        Log.d("modifyAccountBalance", "No such account exists");
    }

    // returns a specific account reference
    public int getAssetBalance(String name)
    {
        for(Accounts acc : assetsList)
        {
            if(acc.getAccountName().equals(name))
            {
                return acc.getAccountBalance();
            }
        }
        Log.d("modifyAccountBalance", "No such account exists");
        return 0;
    }

    // adds an account to the list
    public void addLiability(String accountName, int accountBalance)
    {
        if(!liabilityExists(accountName))
        {
            liabilitiesList.add(new Accounts(accountName, accountBalance));
        }
        Log.d("addAccount", "An account with that name already exists");
    }

    // modifies an account name
    public void modifyLiabilityName(String oldName, String newName)
    {
        for(Accounts acc : liabilitiesList)
        {
            if(acc.getAccountName().equals(oldName))
            {
                acc.setAccountName(newName);
                return;
            }
        }
        Log.d("modifyAccountName", "No such account exists");
    }

    // modifies an account balance
    public void modifyLiabilityBalance(String name, int newBalance)
    {
        for(Accounts acc : liabilitiesList)
        {
            if(acc.getAccountName().equals(name))
            {
                acc.setAccountBalance(newBalance);
                return;
            }
        }
        Log.d("modifyAccountBalance", "No such account exists");
    }

    // returns a specific account reference
    public int getLiabilityBalance(String name)
    {
        for(Accounts acc : liabilitiesList)
        {
            if(acc.getAccountName().equals(name))
            {
                return acc.getAccountBalance();
            }
        }
        Log.d("modifyAccountBalance", "No such account exists");
        return 0;
    }

    // adds a net worth to the list
    public void addNetWorth(int netWorth, String date)
    {
        for(NetWorth entry : netWorthHistory)
        {
            if (entry.getDate().equals(date))
            {
                entry.setNetWorth(netWorth);
                return;
            }
        }

        netWorthHistory.add(new NetWorth(netWorth, date));

    }

    // sets the goal name
    public void setGoalName(String newName)
    {
        goalName = newName;
    }

    // sets the goal amount
    public void setGoalAmount(int goalAmount)
    {
        this.goalAmount = goalAmount;
    }

    // sets the current amount saved
    public void setCurrentAmount(int currentAmount)
    {
        this.currentAmount = currentAmount;
    }

    // sets the flag to indicate whether or not the account is to be used in the goal tracking
    public boolean getGoalFlag(String name)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                return acc.getUseInGoal();
            }
        }
        Log.d("setGoalFlag", "No such account exists");

        return false;
    }

    // sets the flag to indicate whether or not the account is to be used in the goal tracking
    public void setGoalFlag(String name, boolean flag)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                acc.setUseInGoal(flag);
                return;
            }
        }
        Log.d("setGoalFlag", "No such account exists");
    }


    // sets the flag to indicate if the account is an asset
    public void setIsAsset(String name, boolean flag)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                acc.setIsAsset(flag);
                return;
            }
        }
        Log.d("setNetWorthFlag", "No such account exists");
    }

    // sets the flag to indicate if the account is a liability
    public void setIsLiability(String name, boolean flag)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                acc.setIsLiability(flag);
                return;
            }
        }
        Log.d("setNetWorthFlag", "No such account exists");
    }

    // returns if the account is an asset
    public boolean getIsAsset(String name)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                return acc.getIsAsset();
            }
        }
        Log.d("setNetWorthFlag", "No such account exists");
        return false;
    }

    // returns if the account is a liability
    public boolean getIsLiability(String name)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                return acc.getIsLiability();
            }
        }
        Log.d("setNetWorthFlag", "No such account exists");
        return false;
    }

    // returns the total number of accounts
    public int getAccountCount()
    {
        return accountsList.size();
    }

    // returns the total number of accounts
    public int getAssetCount()
    {
        return assetsList.size();
    }

    // returns the total number of accounts
    public int getLiabilityCount()
    {
        return liabilitiesList.size();
    }


    // returns true if there exists an account with that name
    public boolean accountExists(String name)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                return true;
            }
        }
        return false;
    }

    // returns true if there exists an account with that name
    public boolean assetExists(String name)
    {
        for(Accounts acc : assetsList)
        {
            if(acc.getAccountName().equals(name))
            {
                return true;
            }
        }
        return false;
    }

    // returns true if there exists an account with that name
    public boolean liabilityExists(String name)
    {
        for(Accounts acc : liabilitiesList)
        {
            if(acc.getAccountName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
    // deletes an account from the list
    public void deleteAccount(String name)
    {
        for(Accounts acc : accountsList)
        {
            if(acc.getAccountName().equals(name))
            {
                accountsList.remove(acc);
                return;
            }
        }
        Log.d("deleteAccount", "No such account exists");

    }
    // deletes an asset from the list
    public void deleteAsset(String name)
    {
        for(Accounts acc : assetsList)
        {
            if(acc.getAccountName().equals(name))
            {
                assetsList.remove(acc);
                return;
            }
        }
        Log.d("deleteAsset", "No such account exists");

    }

    // deletes a liability from the list
    public void deleteLiability(String name)
    {
        for(Accounts acc : liabilitiesList)
        {
            if(acc.getAccountName().equals(name))
            {
                liabilitiesList.remove(acc);
                return;
            }
        }
        Log.d("deleteLiability", "No such account exists");

    }

    // creates a day 0 net worth so that the user can always use the graph
    private void addFirstNetWorth()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String date = String.format("%tF", calendar);
        
        addNetWorth(0, date);
    }
}
