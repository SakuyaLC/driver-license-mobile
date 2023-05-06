package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "driverLicence.db";
    private static final int DATABASE_VERSION = 1;

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблиц базы данных
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " name TEXT, surname TEXT, middlename TEXT, sex BOOLEAN, email TEXT, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Обновление таблиц базы данных при изменении версии
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}




