package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
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

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public int user_id = -1;
    public String user_name = "", user_surname = "", user_middleName = "", user_PsSerial = "", user_PsNumber = "", user_email = "", user_password = "";
    public boolean user_sex = false;

    OkHttpClient client = new OkHttpClient();

    protected final String сheckURL = "http://192.168.1.33:8080";
    public final String createBlockURL = "http://192.168.1.33:8080/create-block";
    public final String lotteryURL = "http://192.168.1.33:8080/lottery";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        //БД Пользователей приложения
        AppUsersDBHelper appUsersDBHelper = new AppUsersDBHelper(this);
        SQLiteDatabase appUsersDB = appUsersDBHelper.getWritableDatabase();

        //БД ГИБДД
        GibddUsersDBHelper gibddUsersDBHelper = new GibddUsersDBHelper(this);
        SQLiteDatabase gibddUsersDB = gibddUsersDBHelper.getWritableDatabase();

        checkAppUsers();
        checkGibddUsers();

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

        EditText etReg_PsSerial = findViewById(R.id.etReg_PsSerial);
        EditText etReg_PsNumber = findViewById(R.id.etReg_PsNumber);

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
                if (rb_female.isChecked()) {
                    rb_female.setChecked(false);
                    rb_male.setChecked(true);
                    user_sex = false;
                }
            }
        });

        rb_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rb_male.isChecked()) {
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
                user_PsSerial = etReg_PsSerial.getText().toString().trim();
                user_PsNumber = etReg_PsNumber.getText().toString().trim();
                user_email = etReg_Email.getText().toString().trim();
                try {
                    user_password = PasswordEncrypter.encrypt(etReg_Password.getText().toString().trim());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                //Валидация соблюдена
                if (!user_name.equals("") && !user_email.equals("") && !user_password.equals("")) {

                    //Пользователя с такой почтой и такими данными еще нет в системе
                    if (!userEmailExists(user_email) && !userInfoExists(user_name, user_surname, user_middleName, user_sex == false ? "0" : "1")) {

                        //В базе ГИБДД есть запись с такими данными
                        if (licenceExists()) {

                            ContentValues user = new ContentValues();
                            user.put("name", user_name);
                            user.put("surname", user_surname);
                            user.put("middlename", user_middleName);
                            user.put("sex", user_sex);
                            user.put("email", user_email);
                            user.put("password", user_password);
                            user.put("role", getGibddUserRole(Integer.toString(getGibddUserId())));

                            long newUserId = appUsersDB.insert("users", null, user);

                            Gson gson = new Gson();

                            int gibbdUserId = getGibddUserId();
                            Licence licence = getLicenceInfo(gibbdUserId);
                            licence.user_id = newUserId;

                            Message message = new Message();

                            String licenceJson = gson.toJson(licence);

                            message.type = Type.LICENCE;
                            message.content = licenceJson;

                            String messageJson = gson.toJson(message);


                            sendPostRequest(createBlockURL, messageJson, "Что-то пошло не так!", "Блок создан!");
                            sendGetRequest(lotteryURL, "Что-то пошло не так!", "Лотерея началась!");

                            buttonsLayoutHolder.setVisibility(View.VISIBLE);
                            registration_layout.setVisibility(View.GONE);
                            login_layout.setVisibility(View.GONE);

                        } else
                            Toast.makeText(MainActivity.this, "Пользователя с такими данными не существует.", Toast.LENGTH_SHORT).show();

                    } else
                        Toast.makeText(MainActivity.this, "Аккаунт с такими данными уже зарегистрирован!", Toast.LENGTH_SHORT).show();

                } else
                    Toast.makeText(MainActivity.this, "Вы не заполнили некоторые поля!", Toast.LENGTH_SHORT).show();
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
                try {
                    user_password = PasswordEncrypter.encrypt(etLog_Password.getText().toString().trim());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (userLogin(user_email, user_password)) {

                    Intent loginIntent;

                    if (getAppUserRole(getAppUserId(user_email,user_password)).equals("0")) {
                        loginIntent = new Intent(MainActivity.this, AccountPage.class);
                        loginIntent.putExtra("Id", getAppUserId(user_email, user_password));
                        loginIntent.putExtra("Role", getAppUserRole(getAppUserId(user_email, user_password)));
                    } else {
                        loginIntent = new Intent(MainActivity.this, AccountPageEmployee.class);
                        loginIntent.putExtra("Id", getAppUserId(user_email, user_password));
                        loginIntent.putExtra("Role", getAppUserRole(getAppUserId(user_email, user_password)));
                    }

                    appUsersDB.close();
                    gibddUsersDB.close();

                    MainActivity.this.startActivity(loginIntent);
                    MainActivity.this.finish();

                } else Toast.makeText(MainActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();

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

        checkGetResponse();
    }

    public boolean licenceExists() {

        //Database
        GibddUsersDBHelper gibddUsersDBHelper = new GibddUsersDBHelper(this);
        SQLiteDatabase gibddUsersDB = gibddUsersDBHelper.getWritableDatabase();

        Cursor cursor = gibddUsersDB.rawQuery(
                "SELECT * FROM users WHERE name = ? AND surname = ? AND middlename = ? AND ps_serial = ? AND ps_number = ? AND sex = ?",
                new String[]{user_name, user_surname, user_middleName, user_PsSerial, user_PsNumber, user_sex == false ? "0" : "1"});

        boolean exists = cursor.moveToFirst(); // вернет true, если запись существует

        return exists;
    }

    public Licence getLicenceInfo(int uId) {

        //Database
        GibddUsersDBHelper gibddUsersDBHelper = new GibddUsersDBHelper(this);
        SQLiteDatabase gibddUsersDB = gibddUsersDBHelper.getWritableDatabase();

        Licence licence = new Licence();

        Cursor cursor = gibddUsersDB.rawQuery("SELECT * FROM licences" +
                " WHERE user_id = ?", new String[]{Integer.toString(uId)});

        if (cursor.moveToFirst()) {

            licence.user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
            licence.surname = cursor.getString(cursor.getColumnIndexOrThrow("surname"));
            licence.middlename = cursor.getString(cursor.getColumnIndexOrThrow("middlename"));
            licence.birthDatePlace = cursor.getString(cursor.getColumnIndexOrThrow("birthDatePlace"));
            licence.dateOfIssue = cursor.getString(cursor.getColumnIndexOrThrow("dateOfIssue"));
            licence.dateOfExpiration = cursor.getString(cursor.getColumnIndexOrThrow("dateOfExpiration"));
            licence.division = cursor.getString(cursor.getColumnIndexOrThrow("division"));
            licence.code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
            licence.residence = cursor.getString(cursor.getColumnIndexOrThrow("residence"));
            licence.categories = cursor.getString(cursor.getColumnIndexOrThrow("categories"));
        }

        cursor.close();
        gibddUsersDB.close();

        return licence;
    }

    public int getGibddUserId() {

        int uId = -1;

        //Database
        GibddUsersDBHelper gibddUsersDBHelper = new GibddUsersDBHelper(this);
        SQLiteDatabase gibddUsersDB = gibddUsersDBHelper.getWritableDatabase();

        Cursor cursor = gibddUsersDB.rawQuery("SELECT * FROM users" +
                        " WHERE name = ? AND surname = ? and middlename = ? AND ps_serial = ? AND ps_number = ? AND sex = ?",
                new String[]{user_name, user_surname, user_middleName, user_PsSerial, user_PsNumber, user_sex == false ? "0" : "1"});

        if (cursor.moveToFirst()) {
            // Получаем значение id из первой строки результата
            uId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        }

        cursor.close();
        gibddUsersDB.close();

        return uId;
    }

    public String getGibddUserRole(String Id){

        String role = "";

        //Database
        GibddUsersDBHelper gibddUsersDBHelper = new GibddUsersDBHelper(this);
        SQLiteDatabase gibddUsersDB = gibddUsersDBHelper.getWritableDatabase();

        Cursor cursor = gibddUsersDB.rawQuery("SELECT * FROM users" +
                        " WHERE id = ?",
                new String[]{Id});

        if (cursor.moveToFirst()) {
            // Получаем значение id из первой строки результата
            role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
        }

        cursor.close();
        gibddUsersDB.close();

        Log.d("App", "Role is : " + role + " " + (role.equals("0") ? "Пользователь" : "Сотрудник"));

        return role;
    }
    public String getAppUserId(String email, String password){

        int uId = -1;

        //Database
        AppUsersDBHelper dbHelper = new AppUsersDBHelper(this);
        SQLiteDatabase appUsersDB = dbHelper.getWritableDatabase();

        Cursor cursor = appUsersDB.rawQuery("SELECT * FROM users" +
                        " WHERE email = ? AND password = ?",
                new String[]{email, password});

        if (cursor.moveToFirst()) {
            // Получаем значение id из первой строки результата
            uId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        }

        cursor.close();
        appUsersDB.close();

        Log.d("App", "uId is : " + uId);

        return Integer.toString(uId);
    }

    public String getAppUserRole(String Id){

        String role = "";

        //Database
        AppUsersDBHelper dbHelper = new AppUsersDBHelper(this);
        SQLiteDatabase appUsersDB = dbHelper.getWritableDatabase();

        Cursor cursor = appUsersDB.rawQuery("SELECT * FROM users" +
                        " WHERE id = ?",
                new String[]{Id});

        if (cursor.moveToFirst()) {
            // Получаем значение id из первой строки результата
            role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
        }

        cursor.close();
        appUsersDB.close();

        Log.d("App", "Role is : " + role + " " + (role.equals("0") ? "Пользователь" : "Сотрудник"));

        return role;
    }

    public void sendGetRequest(String URL, String onFailureMessage, String onSuccessMessage) {

        Request request;

        request = new Request.Builder()
                .url(URL)
                .get()
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Получаем код ответа
                        int statusCode = response.code();

                        if (statusCode == 200) {
                            Toast.makeText(MainActivity.this, onSuccessMessage + statusCode, Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, onFailureMessage + statusCode, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    public void sendPostRequest(String URL, String message, String onFailureMessage, String onSuccessMessage) {

        Request request;
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", message)
                .build();

        request = new Request.Builder()
                .url(URL)
                .post(requestBody)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Получаем код ответа
                        int statusCode = response.code();

                        if (statusCode == 200) {
                            Toast.makeText(MainActivity.this, onSuccessMessage + statusCode, Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, onFailureMessage + statusCode, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    public void checkAppUsers() {

        //Database
        AppUsersDBHelper dbHelper = new AppUsersDBHelper(this);
        SQLiteDatabase appUsersDB = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM users";
        Cursor cursor = appUsersDB.rawQuery(query, null);
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
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));

                Log.d("DatabaseApp", "User #" + id + " | name: " + name + " | surname: " + surname + " | middlename: " + middlename + " | sex: " + sex + " | email: " + email + " | password: " + password + " | role: " + role);

            } while (cursor.moveToNext());
        }
        cursor.close();
        appUsersDB.close();
    }

    public void checkGibddUsers() {

        //Database
        GibddUsersDBHelper gibddUsersDBHelper = new GibddUsersDBHelper(this);
        SQLiteDatabase gibddUsersDB = gibddUsersDBHelper.getWritableDatabase();

        String query = "SELECT * FROM users";
        Cursor cursor = gibddUsersDB.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                // Получаем значения полей текущей записи
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String surname = cursor.getString(cursor.getColumnIndexOrThrow("surname"));
                String middlename = cursor.getString(cursor.getColumnIndexOrThrow("middlename"));
                String sex = cursor.getString(cursor.getColumnIndexOrThrow("sex"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));

                Log.d("DatabaseGibdd", "User #" + id + " | name: " + name + " | surname: " + surname + " | middlename: " + middlename + " | sex: " + sex + " | role: " + role);

            } while (cursor.moveToNext());
        }
        cursor.close();
        gibddUsersDB.close();
    }

    public boolean userEmailExists(String email) {

        //Database
        AppUsersDBHelper dbHelper = new AppUsersDBHelper(this);
        SQLiteDatabase appUsersDB = dbHelper.getWritableDatabase();

        Cursor cursor = appUsersDB.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});

        boolean exists = cursor.moveToFirst(); // вернет true, если запись существует

        cursor.close();
        appUsersDB.close();

        return exists;
    }

    public boolean userInfoExists(String name, String surname, String middlename, String sex) {

        //Database
        AppUsersDBHelper dbHelper = new AppUsersDBHelper(this);
        SQLiteDatabase appUsersDB = dbHelper.getWritableDatabase();

        Cursor cursor = appUsersDB.rawQuery("SELECT * FROM users WHERE name = ? AND surname = ? AND middlename = ? AND sex = ?",
                new String[]{name, surname, middlename, sex});

        boolean exists = cursor.moveToFirst(); // вернет true, если запись существует

        cursor.close();
        appUsersDB.close();

        return exists;
    }

    public boolean userLogin(String email, String password) {

        //Database
        AppUsersDBHelper dbHelper = new AppUsersDBHelper(this);
        SQLiteDatabase appUsersDB = dbHelper.getWritableDatabase();

        Cursor cursor = appUsersDB.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});

        boolean exists = cursor.moveToFirst(); // вернет true, если запись существует

        cursor.close();
        appUsersDB.close();

        return exists;
    }

    public void checkGetResponse() {
        Request request = new Request.Builder().url(сheckURL).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String responseString = null;
                        Toast toast;

                        try {
                            responseString = response.body().string();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        Log.d("Server", responseString);

                        if (responseString.equals("Success")) {
                            toast = Toast.makeText(getApplicationContext(),
                                    "Соединение есть!", Toast.LENGTH_SHORT);
                        } else {
                            toast = Toast.makeText(getApplicationContext(),
                                    responseString + "Нет соединения", Toast.LENGTH_SHORT);
                        }
                        toast.show();

                    }
                });
            }
        });
    }
}
