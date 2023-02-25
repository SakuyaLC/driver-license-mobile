package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;

public class AccountPage extends AppCompatActivity {

    protected final String CheckURL = "http://10.0.2.2/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);

        Button btn_Profile = findViewById(R.id.btn_Profile);
        Button btn_Shtraf = findViewById(R.id.btn_Shtraf);

        btn_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, CheckURL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Log.d("Response", "Response is " + response);

                            if (response.equals("Success")) {

                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Аккаунт успешно создан.", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(AccountPage.this,error.toString().trim(), Toast.LENGTH_SHORT).show();
                        }
                    }){

                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    requestQueue.add(stringRequest);
                }
        });

        btn_Shtraf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}