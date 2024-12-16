package com.example.financemanagerapp;

public class NetWorth {
    private int netWorth;
    private String date;

    // default constructor needed for firebase
    public NetWorth() {}

    public NetWorth(int netWorth, String date)
    {
        this.netWorth = netWorth;
        this.date = date;
    }

    // returns the net worth
    public int getNetWorth()
    {
        return netWorth;
    }

    // returns the date
    public String getDate()
    {
        return date;
    }

    // sets the net worth
    public void setNetWorth(int netWorth)
    {
        this.netWorth = netWorth;
    }

    // sets the date
    public void setDate(String date)
    {
        this.date = date;
    }


}
