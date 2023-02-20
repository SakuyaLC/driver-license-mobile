package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public String userLoginId, user_name, user_sex, user_email, user_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user_name = "";
        user_sex = "male";
        user_email = "";
        user_password = "";

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
                    user_sex = "male";
                }
            }
        });

        rb_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rb_male.isChecked()){
                    rb_male.setChecked(false);
                    rb_female.setChecked(true);
                    user_sex = "female";
                }
            }
        });

        regLayout_btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_name = etReg_Name.getText().toString().trim();
                user_email = etReg_Email.getText().toString().trim();
                user_password = etReg_Password.getText().toString().trim();

                if (!user_name.equals("") && !user_email.equals("") && !user_password.equals("")){

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
                Intent loginIntent = new Intent(MainActivity.this, AccountPage.class);
                loginIntent.putExtra("userLoginId",userLoginId);
                loginIntent.putExtra("user_name",user_name);
                if (user_sex.equals("male")){
                    loginIntent.putExtra("user_sex","Мужчина");
                } else loginIntent.putExtra("user_sex", "Женщина");
                loginIntent.putExtra("user_email",user_email);
                loginIntent.putExtra("user_password",user_password);
                MainActivity.this.startActivity(loginIntent);
                MainActivity.this.finish();
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
}