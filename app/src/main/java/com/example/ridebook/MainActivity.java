package com.example.ridebook;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private CheckBox checkBox;
    private  Button Login;

    private TextView terms, register;
    private MaterialAlertDialogBuilder materialAlertDialogBuilder;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        checkBox = findViewById(R.id.checkboxterms);
        Login = findViewById(R.id.login_btn);
        terms = findViewById(R.id.textview_register_Terms);
        register = findViewById(R.id.textview_register_link);
        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);

        register.setEnabled(false);
        Login.setEnabled(false);

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TermsAndConditionsActivity.class);
                startActivity(intent);
            }
        });




        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if (b){
                    materialAlertDialogBuilder.setTitle("Terms and Conditions");
                    materialAlertDialogBuilder.setMessage(" By accessing or using the Application, you agree to be bound by these terms. If you do not agree to these terms, please do not use the Application.\n");
                    materialAlertDialogBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            register.setEnabled(true);
                            Login.setEnabled(true);
                            dialog.dismiss();

                        }
                    });

                    materialAlertDialogBuilder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            checkBox.setChecked(false);
                        }
                    });

                    materialAlertDialogBuilder.show();
                }else {
                    register.setEnabled(true);
                    Login.setEnabled(true);
                }
            }

        });

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            Button buttonLogin = findViewById(R.id.login_btn);
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });

            TextView textViewRegister = findViewById(R.id.textview_register_link);
            textViewRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {

    }
}