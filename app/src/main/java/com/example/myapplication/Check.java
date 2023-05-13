package com.example.myapplication;

public class Check {

    long user_id;
    String dateTime;
    CheckType type;
    String description;
    double fine;
}

enum CheckType {
    CHECK,
    FINE
}
