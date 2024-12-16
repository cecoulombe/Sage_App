package com.example.financemanagerapp;

public class Currency {
    private String code;   // 3-letter code
    private String name;   // Full currency name

    // Constructor
    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // returns the three letter code
    public String getCode() {
        return code;
    }

    // returns the full name of the currency
    public String getName() {
        return name;
    }

    // prints the 3 letter code and the full name of the currency
    @Override
    public String toString() {
        return code + " - " + name;
    }
}
