package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.jar.Attributes;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountPage extends AppCompatActivity {

    protected final String CheckURL = "http://10.0.2.2:8080";
    OkHttpClient client = new OkHttpClient();

    public String id, surname, middlename, birthDatePlace, dateOfIssue, dateOfExpiration, division, code, residence, categories;

    public String infoForQr;

    public Intent loginIntent = getIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);

        id = "1"; //loginIntent.getStringExtra("userLoginId");
        surname = "Сорокин"; //loginIntent.getStringExtra("user_surname");
        middlename = "Александр Владимирович"; //loginIntent.getStringExtra("user_name") + " " + loginIntent.getStringExtra("user_middleName");
        birthDatePlace = "21.01.1997 Ростовская облатсь";
        dateOfIssue = "13.04.2009";
        dateOfExpiration = "13.04.2019";
        division = "ГИБДД 6100";
        code = "61 35 414035";
        residence = "Саратовская область";
        categories = "B";

        UpdateInfo();

        Button btn_Profile = findViewById(R.id.btn_Profile);
        Button btn_Shtraf = findViewById(R.id.btn_Shtraf);

        infoForQr = packInfoForQR();
        formQrCode(infoForQr);

        btn_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UpdateInfo();
                infoForQr = packInfoForQR();

                formQrCode(infoForQr);

                checkGetResponse();
            }
        });

        btn_Shtraf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    public void checkGetResponse(){
        Request request = new Request.Builder().url(CheckURL).get().build();
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

                        if (responseString.equals("Success")){
                            toast = Toast.makeText(getApplicationContext(),
                                    "Переход в аккаунт...", Toast.LENGTH_SHORT);
                        }
                        else{
                            toast = Toast.makeText(getApplicationContext(),
                                    responseString + "Нет соединения", Toast.LENGTH_SHORT);
                        }
                        toast.show();

                    }
                });
            }
        });
    }

    public String packInfoForQR(){

        User user = new User(id, surname, middlename, birthDatePlace, dateOfIssue, dateOfExpiration, division, code, residence, categories);

        Gson gson = new Gson();
        String userJson = gson.toJson(user);

        return userJson;
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


}