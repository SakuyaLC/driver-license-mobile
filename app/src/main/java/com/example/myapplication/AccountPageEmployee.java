package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import okhttp3.OkHttpClient;

public class AccountPageEmployee extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient();

    public String id;

    //Водительское удостоверение
    public String surname, middlename, birthDatePlace, dateOfIssue, dateOfExpiration, division, code, residence, categories;

    protected final String localIP = "192.168.64.246";

    //История проверок, штрафов
    String role = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page_employee);

        getSupportActionBar().hide();

        ((ImageView) findViewById(R.id.iv_changeToCam)).setImageResource(R.drawable.camera);

        ((ImageView) findViewById(R.id.iv_changeToCam)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntentAccountPage = new Intent(AccountPageEmployee.this, CameraActivity.class);
                loginIntentAccountPage.putExtra("Id", id);
                AccountPageEmployee.this.startActivity(loginIntentAccountPage);
                AccountPageEmployee.this.finish();
            }
        });

        Intent loginIntent = getIntent();

        id = loginIntent.getStringExtra("Id");
        Log.d("AccountPageIntent", "Got intent with content : " + id);

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

        UpdateInfo();
        formQrCode(id);

        Context contex = this;
        findChecksByUserIdAsync(id, new CheckCallback() {
            @Override
            public void onSuccess(ArrayList<Check> checkList) {
                for (int i = checkList.size() - 1; i >= 0; i--){
                    createCheckUIOnMainThread(checkList.get(i).dateTime, checkList.get(i).type.toString(), checkList.get(i).description, checkList.get(i).fine, contex);
                }
            }

            @Override
            public void onError(Exception exception) {
                // Обработать ошибку
            }
        });

        openProfile();

        Button btn_Profile = findViewById(R.id.btn_Profile);
        Button btn_History = findViewById(R.id.btn_History);
        btn_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UpdateInfo();
                formQrCode(id);
                openProfile();
            }
        });

        btn_History.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                findChecksByUserIdAsync(id, new CheckCallback() {
                    @Override
                    public void onSuccess(ArrayList<Check> checkList) {
                        removeAllChecksFromView();
                        for (int i = checkList.size() - 1; i >= 0; i--){
                            createCheckUIOnMainThread(checkList.get(i).dateTime, checkList.get(i).type.toString(), checkList.get(i).description, checkList.get(i).fine, contex);
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        // Обработать ошибку
                    }
                });

                openHistory();
            }
        });

        findViewById(R.id.iv_Logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logoutIntent = new Intent(AccountPageEmployee.this, MainActivity.class);

                AccountPageEmployee.this.startActivity(logoutIntent);
                AccountPageEmployee.this.finish();
            }
        });

    }

    public void formQrCode(String message){

        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(message, BarcodeFormat.QR_CODE, 512, 512, hints);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ((ImageView) findViewById(R.id.qrCode)).setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public void UpdateInfo(){
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

    public void findBlockByUserIdAsync(String id, final LicenceCallback callback) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                Licence licence;

                //Подключение к MongoDB
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(localIP, 27017))))
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

    public void openProfile(){

        ((Button) findViewById(R.id.btn_Profile)).setBackgroundColor(Color.parseColor("#40C4FF"));
        ((Button) findViewById(R.id.btn_History)).setBackgroundColor(Color.parseColor("#FFFFFF"));

        ((ScrollView) findViewById(R.id.sv_profile)).setVisibility(View.VISIBLE);
        ((ScrollView) findViewById(R.id.sv_history)).setVisibility(View.GONE);
    }

    public void openHistory(){
        ((Button) findViewById(R.id.btn_Profile)).setBackgroundColor(Color.parseColor("#FFFFFF"));
        ((Button) findViewById(R.id.btn_History)).setBackgroundColor(Color.parseColor("#40C4FF"));

        ((ScrollView) findViewById(R.id.sv_profile)).setVisibility(View.GONE);
        ((ScrollView) findViewById(R.id.sv_history)).setVisibility(View.VISIBLE);
    }

    public void createCheckUI(String time, String type, String description, double fine, Context context){

        // Linear Layout body
        LinearLayout ll_body = new LinearLayout(context);
        ll_body.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParamsBody = new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, type.equals("FINE") ? 95 : 70, getResources().getDisplayMetrics()));
        layoutParamsBody.setMargins(0, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()), 0, 0);
        layoutParamsBody.gravity = Gravity.CENTER_VERTICAL;
        ll_body.setLayoutParams(layoutParamsBody);
        ll_body.setBackgroundResource(R.drawable.check_border);

        // Linear Layout time
        LinearLayout ll_time = new LinearLayout(context);
        ll_time.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsTime = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()),
                1f);
        ll_time.setLayoutParams(layoutParamsTime);

        // Linear Layout description
        LinearLayout ll_description = new LinearLayout(context);
        ll_description.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsDescription = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f);
        ll_description.setLayoutParams(layoutParamsDescription);

        //Linear Layout подписи описания
        LinearLayout ll_descriptionLabels = new LinearLayout(context);
        ll_descriptionLabels.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParamsDescriptionLabels = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll_descriptionLabels.setLayoutParams(layoutParamsDescriptionLabels);

        //Linear Layout подписи значения
        LinearLayout ll_descriptionValues = new LinearLayout(context);
        ll_descriptionValues.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParamsDescriptionValues = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParamsDescriptionValues.gravity = Gravity.RIGHT;
        ll_descriptionValues.setLayoutParams(layoutParamsDescriptionValues);
        ll_descriptionValues.setPadding(0, 0, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), 0);


        //Тип, описание и размер штрафа (Подписи)
        TextView label_type = new TextView(context);
        label_type.setText("Тип: ");
        LinearLayout.LayoutParams layoutParamsLabelType = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsLabelType.setMargins((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()), 0, 0);
        label_type.setLayoutParams(layoutParamsLabelType);
        label_type.setTextColor(Color.parseColor("#151515"));
        label_type.setTypeface(null, Typeface.BOLD);

        TextView label_description = new TextView(context);
        label_description.setText("Описание: ");
        label_description.setLayoutParams(layoutParamsLabelType);
        label_description.setTextColor(Color.parseColor("#151515"));
        label_description.setTypeface(null, Typeface.BOLD);

        TextView label_fine = new TextView(context);
        label_fine.setText("Размер: ");
        label_fine.setLayoutParams(layoutParamsLabelType);
        label_fine.setTextColor(Color.parseColor("#151515"));
        label_fine.setTypeface(null, Typeface.BOLD);

        //Время (Значения)
        TextView tv_time = new TextView(context);
        tv_time.setText(time);
        tv_time.setLayoutParams(layoutParamsLabelType);
        tv_time.setTextColor(Color.parseColor("#151515"));
        tv_time.setTypeface(null, Typeface.BOLD);

        //Тип, описание и размер штрафа (Значения)
        TextView tv_type = new TextView(context);
        tv_type.setText(type.equals("CHECK") ? "Проверка": "Штраф");
        LinearLayout.LayoutParams layoutParamsValueType = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsValueType.setMargins((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()), 0, 0);
        tv_type.setLayoutParams(layoutParamsValueType);
        tv_type.setTextColor(Color.parseColor("#151515"));
        tv_type.setTypeface(null, Typeface.BOLD);

        TextView tv_description = new TextView(context);
        tv_description.setText(description);
        tv_description.setLayoutParams(layoutParamsLabelType);
        tv_description.setTextColor(Color.parseColor("#151515"));
        tv_description.setTypeface(null, Typeface.BOLD);

        TextView tv_fine = new TextView(context);
        if (type.equals("FINE")){
            tv_fine.setText(Double.toString(fine) + " рублей");
        } else {
            label_fine.setVisibility(View.GONE);
            tv_fine.setVisibility(View.GONE);
        }
        tv_fine.setLayoutParams(layoutParamsLabelType);
        tv_fine.setTextColor(Color.parseColor("#151515"));
        tv_fine.setTypeface(null, Typeface.BOLD);

        //Вставка времени в layout времени
        ll_time.addView(tv_time);

        //Вставка значений в layout подписей
        ll_descriptionLabels.addView(label_type);
        ll_descriptionLabels.addView(label_description);
        ll_descriptionLabels.addView(label_fine);

        //Вставка значений в layout значений
        ll_descriptionValues.addView(tv_type);
        ll_descriptionValues.addView(tv_description);
        ll_descriptionValues.addView(tv_fine);

        //Вставка layout'ов в layout horizontal для описания
        ll_description.addView(ll_descriptionLabels);
        ll_description.addView(ll_descriptionValues);

        //Вставка всех layout'ов в соответствующий держатель
        ll_body.addView(ll_time);
        ll_body.addView(ll_description);

        //Вставка держателя в scroll view истории
        LinearLayout scrollView = (LinearLayout) findViewById(R.id.svH_body);
        scrollView.addView(ll_body);
    }

    public void findChecksByUserIdAsync(String id, final CheckCallback callback) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                ArrayList<Check> checkList = new ArrayList<>();
                Check check;

                //Подключение к MongoDB
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(localIP, 27017))))
                        .build();

                MongoClient mongoClient = MongoClients.create(settings);

                MongoDatabase database = mongoClient.getDatabase("blockchainDB");

                MongoCollection<Document> collection = database.getCollection("blocks");

                MongoCursor<Document> cursor = collection.find().iterator();
                if (cursor.hasNext()) {
                    cursor.next();
                    while (cursor.hasNext()) {

                        Document block = cursor.next();

                        String info = block.getString("info");

                        Gson gson = new Gson();
                        Message message = gson.fromJson(info, Message.class);

                        if (message.type.toString().equals("CHECK")) {

                            check = gson.fromJson(message.content, Check.class);

                            if (Long.toString(check.user_id).equals(id)) {
                                checkList.add(check);
                                Log.d("MongoDB", "Check found! It's id :" + Long.toString(check.user_id));
                            }
                        }
                    }
                    callback.onSuccess(checkList);
                }

                callback.onError(new Exception("Licence not found for user ID: " + id));
            }
        }).start();
    }

    public interface CheckCallback {
        void onSuccess(ArrayList<Check> checkList);
        void onError(Exception exception);
    }

    private void createCheckUIOnMainThread(final String dateTime, final String type, final String description, final double fine, final Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createCheckUI(dateTime, type, description, fine, context);
            }
        });
    }

    private void removeAllChecksFromView(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((LinearLayout) findViewById(R.id.svH_body)).removeAllViews();
            }
        });
    }


}