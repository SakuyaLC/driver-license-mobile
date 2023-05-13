package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GibddUsersDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "gibdd.db";
    private static final int DATABASE_VERSION = 6;

    public GibddUsersDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Создание таблиц базы данных
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " name TEXT, surname TEXT, middlename TEXT, sex BOOLEAN,"
                + " ps_serial TEXT, ps_number TEXT, birthDate TEXT, birthPlace TEXT, residence TEXT, role INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE licences (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,"
                + " surname TEXT, middlename TEXT, birthDatePlace TEXT,"
                + " dateOfIssue TEXT, dateOfExpiration TEXT, division TEXT, code TEXT, residence TEXT, categories TEXT)");

        for (int i = 1; i < 10; i++){
            ContentValues user = new ContentValues();
            user.put("name", "И" + i);
            user.put("surname", "Ф" + i);
            user.put("middlename", "О" + i);
            user.put("sex", i%2==0 ? true:false);
            user.put("ps_serial", "631" + i);
            user.put("ps_number", "91999" + i);
            user.put("birthDate", "0" + i + ".01.199" + i);
            user.put("birthPlace", "Место рождения # " + i);
            user.put("residence", "Регион проживания #" + i);

            db.insert("users", null, user);
        }

        for (int i = 1; i < 10; i++){
            ContentValues licence = new ContentValues();
            licence.put("user_id", i);
            licence.put("surname", "Фамилия #" + i);
            licence.put("middlename", "Имя Отчество # " + i);
            licence.put("birthDatePlace", "0" + i + ".01.199" + i + " Место рождения # " + i);
            licence.put("dateOfIssue", "0" + i + ".01.200" + i);
            licence.put("dateOfExpiration", "0" + i + ".01.201" + i);
            licence.put("division", "ГИБДД 6" + i + "00");
            licence.put("code", "61 35 4" + i + "4035");
            licence.put("residence", "Регион проживания #" + i);
            licence.put("categories", "B1, CE");

            db.insert("licences", null, licence);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Обновление таблиц базы данных при изменении версии
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS licences");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        // Удаление таблиц базы данных при понижении версии
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS licences");
        onCreate(db);
    }
}
