package com.example.ridebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import Fragments.ProfileFragment;

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private EditText editTextCurrentPwd, editTextNewPwd, editTextConfPwd;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd, buttonReAuthenticate;
    private ProgressBar progressBar;
    private String userPwdCurr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_change_password);


        editTextCurrentPwd = findViewById(R.id.edittext_change_pwd_current);
        editTextNewPwd = findViewById(R.id.edittext_change_pwd_new);
        editTextConfPwd = findViewById(R.id.edittext_change_pwd_new_confirm);
        textViewAuthenticated = findViewById(R.id.textview_change_pwd_authenticated);
        progressBar = findViewById(R.id.progressBar);
        buttonReAuthenticate = findViewById(R.id.button_change_pwd_authenticate);
        buttonChangePwd = findViewById(R.id.button_change_pwd);

        editTextNewPwd.setEnabled(false);
        editTextConfPwd.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(ChangePasswordActivity.this , "Something went wrong! User's details is not available",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this, ProfileFragment.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwdCurr = editTextCurrentPwd.getText().toString();

                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePasswordActivity.this, "Password is needed", Toast.LENGTH_SHORT).show();
                    editTextCurrentPwd.setError("Please enter your current passwrod");
                    editTextCurrentPwd.requestFocus();
                }else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwdCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                editTextCurrentPwd.setEnabled(false);
                                editTextNewPwd.setEnabled(true);
                                editTextConfPwd.setEnabled(true);

                                buttonChangePwd.setEnabled(true);
                                buttonReAuthenticate.setEnabled(false);

                                textViewAuthenticated.setText("You are authenticated/verified"+
                                        "You can change password now!");
                                Toast.makeText(ChangePasswordActivity.this, "Password has been verified", Toast.LENGTH_SHORT).show();

                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(ChangePasswordActivity.this, R.color.dark_green));

                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            }else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userPwdNew = editTextNewPwd.getText().toString();
        String userPwdConfirmNew = editTextConfPwd.getText().toString();

        if(TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "New password is required", Toast.LENGTH_SHORT).show();
            editTextNewPwd.setError("Please enter your new password");
            editTextNewPwd.requestFocus();
        }else if (TextUtils.isEmpty(userPwdConfirmNew)) {
            Toast.makeText(ChangePasswordActivity.this, "", Toast.LENGTH_SHORT).show();
            editTextConfPwd.setError("Please re-enter your new password");
            editTextConfPwd.requestFocus();
        }else if (!userPwdNew.matches(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this, "Password did not match", Toast.LENGTH_SHORT).show();
            editTextConfPwd.setError("Please re-enter your new password");
            editTextConfPwd.requestFocus();
        }else if (userPwdCurr.matches(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "New password cannot be same as old password.", Toast.LENGTH_SHORT).show();
            editTextNewPwd.setError("Please re-enter your new password");
            editTextNewPwd.requestFocus();
        }else {
            progressBar.setVisibility(View.GONE);

            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePasswordActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }
}