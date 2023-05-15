package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckActivity extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient();

    public String surname, middlename, birthDatePlace, dateOfIssue, dateOfExpiration, division, code, residence, categories = " ";

    public final String createBlockURL = "http://10.0.2.2:80/create-block";
    public final String lotteryURL = "http://10.0.2.2:80/lottery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        Intent qrScanIntent = getIntent();
        String id = qrScanIntent.getStringExtra("Result");

        findBlockByUserIdAsync(id, new LicenceCallback() {
            @Override
            public void onSuccess(Licence licence) {

                surname = licence.surname;
                middlename = licence.middlename;
                birthDatePlace = licence.birthDatePlace;
                dateOfIssue = licence.dateOfIssue;
                dateOfExpiration = licence.dateOfExpiration;
                division = licence.division;
                code = licence.code;
                residence = licence.residence;
                categories = licence.categories;

                UpdateInfo();

                Log.d("MongoDB", "Licence is loaded...");
            }

            @Override
            public void onError(Exception exception) {
                // Обработать ошибку
            }
        });

        RadioButton rb_check = findViewById(R.id.rb_check);
        RadioButton rb_fine = findViewById(R.id.rb_fine);

        rb_check.setChecked(true);
        ((TextView) findViewById(R.id.tvCheck_Fine)).setVisibility(View.GONE);
        ((EditText) findViewById(R.id.etCheck_Fine)).setVisibility(View.GONE);

        rb_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rb_fine.isChecked()) {
                    ((TextView) findViewById(R.id.tvCheck_Fine)).setVisibility(View.GONE);
                    ((EditText) findViewById(R.id.etCheck_Fine)).setVisibility(View.GONE);
                    rb_fine.setChecked(false);
                    rb_check.setChecked(true);
                }
            }
        });

        rb_fine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rb_check.isChecked()) {
                    ((TextView) findViewById(R.id.tvCheck_Fine)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.etCheck_Fine)).setVisibility(View.VISIBLE);
                    rb_check.setChecked(false);
                    rb_fine.setChecked(true);
                }
            }
        });

        Button btn_Cancel = findViewById(R.id.btn_Cancel);
        Button btn_ConfirmCheck = findViewById(R.id.btn_СonfirmCheck);

        EditText etCheck_Description = findViewById(R.id.etCheck_Description);
        EditText etCheck_Fine = findViewById(R.id.etCheck_Fine);

        btn_ConfirmCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("CheckActivity", etCheck_Description.getText().toString().trim());
                Log.d("CheckActivity", etCheck_Fine.getText().toString().trim());
                Log.d("CheckActivity", Double.toString(Double.parseDouble(etCheck_Fine.getText().toString().trim())));

                if (!etCheck_Description.getText().toString().equals("")) {
                    if (rb_check.isChecked()) {
                            Check check = new Check();

                            check.user_id = Long.parseLong(id);

                            if (rb_check.isChecked()) check.type = CheckType.CHECK;
                            else check.type = CheckType.FINE;
                            // Создаем объект SimpleDateFormat с нужным шаблоном
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");

                            // Получаем текущую дату
                            Date currentDate = new Date();

                            // Применяем форматирование и выводим результат
                            String formattedDate = dateFormat.format(currentDate);
                            check.dateTime = formattedDate;
                            check.description = etCheck_Description.getText().toString().trim();
                            if (check.type == CheckType.CHECK) check.fine = 0.0;
                            else
                                check.fine = Double.parseDouble(etCheck_Fine.getText().toString().trim());

                            Gson gson = new Gson();
                            Message message = new Message();
                            message.type = Type.CHECK;
                            message.content = gson.toJson(check);

                            String messageJson = gson.toJson(message);

                            sendPostRequest(createBlockURL, messageJson, "Что-то пошло не так!", "Блок создан");
                            sendGetRequest(lotteryURL, "Что-то пошло не так", "Блок создан");

                            Intent loginIntent = new Intent(CheckActivity.this, AccountPageEmployee.class);
                            loginIntent.putExtra("Id", qrScanIntent.getStringExtra("Id"));

                            CheckActivity.this.startActivity(loginIntent);
                            CheckActivity.this.finish();
                    } else if (rb_fine.isChecked() && Double.parseDouble(etCheck_Fine.getText().toString().trim()) > 0.0){
                        Check check = new Check();

                        check.user_id = Long.parseLong(id);

                        if (rb_check.isChecked()) check.type = CheckType.CHECK;
                        else check.type = CheckType.FINE;
                        // Создаем объект SimpleDateFormat с нужным шаблоном
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");

                        // Получаем текущую дату
                        Date currentDate = new Date();

                        // Применяем форматирование и выводим результат
                        String formattedDate = dateFormat.format(currentDate);
                        check.dateTime = formattedDate;
                        check.description = etCheck_Description.getText().toString().trim();
                        if (check.type == CheckType.CHECK) check.fine = 0.0;
                        else
                            check.fine = Double.parseDouble(etCheck_Fine.getText().toString().trim());

                        Gson gson = new Gson();
                        Message message = new Message();
                        message.type = Type.CHECK;
                        message.content = gson.toJson(check);

                        String messageJson = gson.toJson(message);

                        sendPostRequest(createBlockURL, messageJson, "Что-то пошло не так!", "Блок создан");
                        sendGetRequest(lotteryURL, "Что-то пошло не так", "Блок создан");

                        Intent loginIntent = new Intent(CheckActivity.this, AccountPageEmployee.class);
                        loginIntent.putExtra("Id", qrScanIntent.getStringExtra("Id"));

                        CheckActivity.this.startActivity(loginIntent);
                        CheckActivity.this.finish();
                    } else Toast.makeText(CheckActivity.this, "Вы не заполнили некоторые поля или заполнили их неправильно!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntentCamera = getIntent();

                Intent loginIntent = new Intent(CheckActivity.this, AccountPageEmployee.class);
                loginIntent.putExtra("Id", loginIntentCamera.getStringExtra("Id"));

                CheckActivity.this.startActivity(loginIntent);
                CheckActivity.this.finish();
            }
        });

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
                            Toast.makeText(CheckActivity.this, onSuccessMessage + statusCode, Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(CheckActivity.this, onFailureMessage + statusCode, Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(CheckActivity.this, onSuccessMessage + statusCode, Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(CheckActivity.this, onFailureMessage + statusCode, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    public void findBlockByUserIdAsync(String id, final CheckActivity.LicenceCallback callback) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                Licence licence;

                //Подключение к MongoDB
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress("10.0.2.2", 27017))))
                        .build();

                MongoClient mongoClient = MongoClients.create(settings);

                MongoDatabase database = mongoClient.getDatabase("blockchainDB");

                MongoCollection<Document> collection = database.getCollection("blocks");
                Log.d("MongoDB", "Подключение есть!");

                MongoCursor<Document> cursor = collection.find().iterator();
                if (cursor.hasNext()) {
                    cursor.next();
                    while (cursor.hasNext()) {

                        Document block = cursor.next();

                        String info = block.getString("info");

                        Gson gson = new Gson();
                        Message message = gson.fromJson(info, Message.class);

                        if (message.type.toString().equals("LICENCE")) {

                            licence = gson.fromJson(message.content, Licence.class);

                            if (Long.toString(licence.user_id).equals(id)) {
                                callback.onSuccess(licence);
                                Log.d("MongoDB", "Licence found! It's id :" + Long.toString(licence.user_id));
                                return;
                            }
                        }
                    }
                }

                callback.onError(new Exception("Licence not found for user ID: " + id));
            }
        }).start();
    }

    public interface LicenceCallback {
        void onSuccess(Licence licence);

        void onError(Exception exception);
    }

    public void UpdateInfo() {
        //Поля в профиле пользователя
        TextView tv_UserSurname = findViewById(R.id.tv_UserSurname);
        TextView tv_UserMiddleName = findViewById(R.id.tv_UserMiddleName);
        TextView tv_UserBirthDatePlace = findViewById(R.id.tv_UserBirthDatePlace);
        TextView tv_UserDateOfIssue = findViewById(R.id.tv_UserDateOfIssue);
        TextView tv_UserDateOfExpiration = findViewById(R.id.tv_UserDateOfExpiration);
        TextView tv_UserDivision = findViewById(R.id.tv_UserDivision);
        TextView tv_UserCode = findViewById(R.id.tv_UserCode);
        TextView tv_UserRegionOfResidence = findViewById(R.id.tv_UserRegionOfResidence);
        TextView tv_UserCategories = findViewById(R.id.tv_UserCategories);

        tv_UserSurname.setText(surname);
        tv_UserMiddleName.setText(middlename);
        tv_UserBirthDatePlace.setText(birthDatePlace);
        tv_UserDateOfIssue.setText(dateOfIssue);
        tv_UserDateOfExpiration.setText(dateOfExpiration);
        tv_UserDivision.setText(division);
        tv_UserCode.setText(code);
        tv_UserRegionOfResidence.setText(residence);
        tv_UserCategories.setText(categories);
    }
}