package com.example.ridebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import models.ModelUsers;

public class SettingsActivity extends AppCompatActivity {
    private Button updateprofile, updateemail, changepassword, deleteprofile;

    ImageView menu;

    DrawerLayout drawerLayout;

    private FirebaseAuth authProfile;

    LinearLayout settings, emergency, findmyparking, logout;

    private ImageView dp;

    private TextView name, email;

    private String fullname, Email;

    SwitchCompat postSwitch;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static  final  String TOPIC_POST_NOTIFICATION = "POST";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        menu = findViewById(R.id.menu);
        postSwitch = findViewById(R.id.postSwitch);

        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean(""+ TOPIC_POST_NOTIFICATION, false);
        if(isPostEnabled){
            postSwitch.setChecked(true);
        }
        else {
            postSwitch.setChecked(false);
        }
        updateemail = findViewById(R.id.Button_Update_Email);
        changepassword = findViewById(R.id.Button_Change_Password);
        deleteprofile = findViewById(R.id.Button_Delete_Profile);
        drawerLayout = findViewById(R.id.drawerLayout);
        settings = findViewById(R.id.settings);
        emergency = findViewById(R.id.emergency);
        findmyparking = findViewById(R.id.findmyp);
        logout = findViewById(R.id.logout);
        dp = findViewById(R.id.dp);
        name = findViewById(R.id.navname);
        email = findViewById(R.id.navemail);
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(SettingsActivity.this, "Something went wrong! User's details are not available at the moment", Toast.LENGTH_LONG).show();
        } else {
            showUserProfile(firebaseUser);
        }

        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                editor = sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION,isChecked);
                editor.apply();

                if(isChecked){
                    subscribePostNotification();
                }
                else{

                    unsubscribePostNotification();

                }
            }
        });


        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });


        updateemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        changepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        deleteprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, DeleteProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recreate();

            }
        });

        findmyparking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                redirectActivity(SettingsActivity.this, FindMyParkingActivity.class);

            }
        });

        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emergencyNumber = "911"; // Change this to the desired emergency number

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + emergencyNumber));

                if (ActivityCompat.checkSelfPermission(SettingsActivity.this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authProfile.signOut();
                Toast.makeText(SettingsActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        });
    }

    private void unsubscribePostNotification() {

        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+ TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will not receive post notification";
                        if(!task.isSuccessful()){
                            msg = "UnSubscription failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void subscribePostNotification() {

        FirebaseMessaging.getInstance().subscribeToTopic(""+ TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will received post notification";
                        if(!task.isSuccessful()){
                            msg = "Subscription failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers modelUsers = snapshot.getValue(ModelUsers.class);
                if (modelUsers != null) {
                    fullname = firebaseUser.getDisplayName();
                    Email = firebaseUser.getEmail();

                    name.setText(fullname);
                    email.setText(Email);

                    // Load the profile picture using Picasso
                    if (modelUsers.getImage() != null && !modelUsers.getImage().isEmpty()) {
                        String imageUrl = modelUsers.getImage();
                        Picasso.get().load(imageUrl).resize(300, 300).centerCrop().into(dp);
                    } else {
                    }

                } else {
                    Toast.makeText(SettingsActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void openDrawer (DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void closeDrawer(DrawerLayout drawerLayout){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingsActivity.this,DashboardActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}