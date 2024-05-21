package com.example.ridebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

public class UpdateEmailActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private String userOldEmail, userNewEmail, userPwd;
    private Button buttonUpdateEmail;
    private EditText editTextEmail, editTextPwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_update_email);

        progressBar = findViewById(R.id.progressBar);
        editTextPwd = findViewById(R.id.edittext_update_email_verify_password);
        editTextEmail = findViewById(R.id.edittext_update_email_new);
        textViewAuthenticated = findViewById(R.id.textview_update_email_authenticated);
        buttonUpdateEmail = findViewById(R.id.button_update_email);

        buttonUpdateEmail.setEnabled(false);
        editTextEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.textview_update_email_old);
        textViewOldEmail.setText(userOldEmail);

        if (firebaseUser.equals("")){
            Toast.makeText(UpdateEmailActivity.this, "Something went wrong! User's details not available", Toast.LENGTH_SHORT).show();
        } else {
            reAuthenticate(firebaseUser);
        }
    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button buttonVerifyUser = findViewById(R.id.button_authenticate_user);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userPwd = editTextPwd.getText().toString();

                if (TextUtils.isEmpty(userPwd)){
                    Toast.makeText(UpdateEmailActivity.this, "Password is needed to continue", Toast.LENGTH_SHORT).show();
                    editTextPwd.setError("Please enter your password for authentication");
                    editTextPwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPwd);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(UpdateEmailActivity.this, "Password has been verified" +
                                            "You can update email now", Toast.LENGTH_SHORT).show();

                                    textViewAuthenticated.setText("You are authenticated. You can update your email now");

                                    editTextEmail.setEnabled(true);
                                    editTextPwd.setEnabled(false);
                                    buttonVerifyUser.setEnabled(false);
                                    buttonUpdateEmail.setEnabled(true);

                                    buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this,
                                            R.color.dark_green));

                                    buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            userNewEmail = editTextEmail.getText().toString();
                                            if(TextUtils.isEmpty(userNewEmail)){
                                                Toast.makeText(UpdateEmailActivity.this, "New email is required", Toast.LENGTH_SHORT).show();
                                                editTextEmail.setError("Please enter new email");
                                                editTextEmail.requestFocus();
                                            } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                                Toast.makeText(UpdateEmailActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                                                editTextEmail.setError("Please provide valid Email");
                                                editTextEmail.requestFocus();
                                            }else if (userOldEmail.matches(userNewEmail)){
                                                Toast.makeText(UpdateEmailActivity.this, "New Email cannot be same as old email", Toast.LENGTH_SHORT).show();
                                                editTextEmail.setError("Please provide valid Email");
                                                editTextEmail.requestFocus();
                                            }else {
                                                progressBar.setVisibility(View.GONE);
                                                updateEmail(firebaseUser);
                                            }
                                        }
                                    });
                                }
                            } else {
                                try {
                                    throw task.getException();

                                } catch (Exception e){
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }

            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()){
                    firebaseUser.sendEmailVerification();

                    Toast.makeText(UpdateEmailActivity.this, "Email has been updated. Please verify your new email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmailActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                progressBar.setVisibility(View.GONE);
            }
        });
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