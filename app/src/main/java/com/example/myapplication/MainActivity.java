package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public int user_id = 1;
    public String user_name = "Алексей", user_surname = "Воробьев", user_middleName = "Родионович", user_email = "alexey@mail.ru", user_password = "123";
    public boolean user_sex = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Database
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        checkUsers();

        //Button holder
        LinearLayout buttonsLayoutHolder = findViewById(R.id.buttonsLayoutHolder);
        LinearLayout registration_layout = findViewById(R.id.registration_layout);
        LinearLayout login_layout = findViewById(R.id.login_layout);
        Button btn_openRegistration = findViewById(R.id.btn_openRegistration);
        Button btn_openLogin = findViewById(R.id.btn_openLogin);

        registration_layout.setVisibility(View.GONE);
        login_layout.setVisibility(View.GONE);
        buttonsLayoutHolder.setVisibility(View.VISIBLE);

        btn_openRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsLayoutHolder.setVisibility(View.GONE);
                login_layout.setVisibility(View.GONE);
                registration_layout.setVisibility(View.VISIBLE);
            }
        });

        btn_openLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsLayoutHolder.setVisibility(View.GONE);
                login_layout.setVisibility(View.VISIBLE);
                registration_layout.setVisibility(View.GONE);
            }
        });

        //Registration layout
        EditText etReg_Name = findViewById(R.id.etReg_Name);
        EditText etReg_Surname = findViewById(R.id.etReg_Surname);
        EditText etReg_MiddleName = findViewById(R.id.etReg_MiddleName);

        RadioButton rb_male = findViewById(R.id.rb_male);
        RadioButton rb_female = findViewById(R.id.rb_female);

        EditText etReg_Email = findViewById(R.id.etReg_Email);
        EditText etReg_Password = findViewById(R.id.etReg_Password);

        Button regLayout_btnReg = findViewById(R.id.regLayout_btnReg);
        Button btn_regCancel = findViewById(R.id.btn_regCancel);

        rb_male.setChecked(true);

        rb_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rb_female.isChecked()){
                    rb_female.setChecked(false);
                    rb_male.setChecked(true);
                    user_sex = false;
                }
            }
        });

        rb_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rb_male.isChecked()){
                    rb_male.setChecked(false);
                    rb_female.setChecked(true);
                    user_sex = true;
                }
            }
        });

        regLayout_btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_name = etReg_Name.getText().toString().trim();
                user_surname = etReg_Surname.getText().toString().trim();
                user_middleName = etReg_MiddleName.getText().toString().trim();
                user_email = etReg_Email.getText().toString().trim();
                user_password = etReg_Password.getText().toString().trim();

                if (!user_name.equals("") && !user_email.equals("") && !user_password.equals("")){

                    if (!userExists(user_email)){
                        ContentValues values = new ContentValues();
                        values.put("name", user_name);
                        values.put("surname", user_surname);
                        values.put("middlename", user_middleName);
                        values.put("sex", user_sex);
                        values.put("email", user_email);
                        values.put("password", user_password);

                        long newRowId = db.insert("users", null, values);

                        db.close();

                        Toast.makeText(MainActivity.this,"Успешная регистрация", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(MainActivity.this,"Аккаунт с такой почтой уже зарегистрирован!", Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(MainActivity.this,"Вы не заполнили некоторые поля!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_regCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsLayoutHolder.setVisibility(View.VISIBLE);
                registration_layout.setVisibility(View.GONE);
                login_layout.setVisibility(View.GONE);
            }
        });

        //Login layout
        EditText etLog_Email = findViewById(R.id.etLog_Email);
        EditText etLog_Password = findViewById(R.id.etLog_Password);
        Button logLayout_btnLogin = findViewById(R.id.logLayout_btnLogin);
        Button btn_logCancel = findViewById(R.id.btn_logCancel);

        logLayout_btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                user_email = etLog_Email.getText().toString();
                user_password = etLog_Password.getText().toString();

                if (userLogin(user_email, user_password)){

                    Intent loginIntent = new Intent(MainActivity.this, AccountPage.class);
                    loginIntent.putExtra("userLoginId",user_id);

                    if(user_name != null && !user_name.isEmpty())
                        loginIntent.putExtra("user_name",user_name);

                    if(user_surname != null && !user_surname.isEmpty())
                        loginIntent.putExtra("user_surname",user_surname);

                    if(user_middleName != null && !user_middleName.isEmpty())
                        loginIntent.putExtra("user_middleName",user_middleName);

                    if (!user_sex){
                        loginIntent.putExtra("user_sex",false);
                    } else loginIntent.putExtra("user_sex", true);

                    if(user_email != null && !user_email.isEmpty())
                        loginIntent.putExtra("user_email",user_email);

                    if(user_password != null && !user_password.isEmpty())
                        loginIntent.putExtra("user_password",user_password);

                    MainActivity.this.startActivity(loginIntent);
                    MainActivity.this.finish();

                } else Toast.makeText(MainActivity.this,"Неверный логин или пароль", Toast.LENGTH_SHORT).show();


            }

            });
        btn_logCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsLayoutHolder.setVisibility(View.VISIBLE);
                registration_layout.setVisibility(View.GONE);
                login_layout.setVisibility(View.GONE);
            }
        });
    }

    public void checkUsers(){

        //Database
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM users";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                // Получаем значения полей текущей записи
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String surname = cursor.getString(cursor.getColumnIndexOrThrow("surname"));
                String middlename = cursor.getString(cursor.getColumnIndexOrThrow("middlename"));
                String sex = cursor.getString(cursor.getColumnIndexOrThrow("sex"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));

                Log.d("Database", "User #" + id + " | name: " + name + " | surname: " + surname + " | middlename: " + middlename + " | sex=" + sex + " | email=" + email + " | password=" + password);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    public boolean userExists(String email){

        //Database
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});

        boolean exists = cursor.moveToFirst(); // вернет true, если запись существует

        cursor.close();

        return exists;
    }

    public boolean userLogin(String email, String password){

        //Database
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});

        boolean exists = cursor.moveToFirst(); // вернет true, если запись существует

        cursor.close();

        return exists;
    }
}
