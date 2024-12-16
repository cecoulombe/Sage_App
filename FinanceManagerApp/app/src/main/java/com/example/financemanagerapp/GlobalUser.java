package com.example.financemanagerapp;

// allows the user's data to be used across the app
public class GlobalUser {
    private static User user;

    // setter for the User object
    public static void setUser(User u)
    {
        user = u;
    }

    // getter for the User object
    public static User getUser()
    {
        return user;
    }
}
