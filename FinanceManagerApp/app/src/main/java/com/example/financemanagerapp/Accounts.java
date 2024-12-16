package com.example.financemanagerapp;

//Objects representing an individual bank account with a name, balance, and flag indicating if it is used in the goal tracking
public class Accounts {

    // Atrtibutes
    String accountName;
    int accountBalance; // using an integer to represent the actual balance * 100 so that it doens't lose precision
    boolean useInGoal;
    boolean isAsset;
    boolean isLiability;

    // constructor for firebase
    public Accounts() {}

    // constructor to create an account with a specific name and balance if passed an integer
    public Accounts(String accountName, int accountBalance)
    {
        this.accountName = accountName;
        this.accountBalance = accountBalance;
        this.useInGoal = false;
        this.isAsset = false;
        this.isLiability = false;
    }

    // returns the account name
    public String getAccountName()
    {
        return accountName;
    }

    // returns the account balance
    public int getAccountBalance()
    {
        return accountBalance;
    }

    // returns the goal flag
    public boolean getUseInGoal()
    {
        return useInGoal;
    }

    // returns the asset flag
    public boolean getIsAsset()
    {
        return isAsset;
    }

    // return the liability flag
    public boolean getIsLiability()
    {
        return isLiability;
    }

    // sets the account name
    public void setAccountName(String newName)
    {
        accountName = newName;
    }

    // sets the account balance (from an int)
    public void setAccountBalance(int newBalance)
    {
        accountBalance = newBalance;
    }

    // sets the goal flag
    public void setUseInGoal(boolean flag)
    {
        useInGoal = flag;
    }

    // sets the asset flag
    public void setIsAsset(boolean flag)
    {
        isAsset = flag;
    }

    // sets the liability flag
    public void setIsLiability(boolean flag)
    {
        isLiability = flag;
    }
}
