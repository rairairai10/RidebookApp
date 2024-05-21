package com.example.ridebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import Fragments.ChatListFragment;
import Fragments.GroupChatsFragment;
import Fragments.HomeFragment;
import Fragments.NotificationsFragment;
import Fragments.ProfileFragment;
import Fragments.UsersFragment;
import models.ModelUsers;
import notifications.Token;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    DrawerLayout drawerLayout;

    ImageView menu,dp;

    private String name, Email;

    private TextView Name, email;


    LinearLayout rides,settings, emergency, findmyparking,contact, about, privacy, logout;


    static String mUID;
    private BottomNavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        actionBar= getSupportActionBar();
        actionBar.setTitle("Profile");

         drawerLayout =findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        settings = findViewById(R.id.settings);

        emergency = findViewById(R.id.emergency);
        findmyparking = findViewById(R.id.findmyp);
        contact = findViewById( R.id.Contact);
        about = findViewById(R.id.About);
        logout = findViewById(R.id.logout);
        Name = findViewById(R.id.navname);
        dp = findViewById(R.id.dp);
        email = findViewById(R.id.navemail);
        privacy = findViewById(R.id.Privacy);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        actionBar.setTitle("Home");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

        checkUserStatus();


        if (firebaseUser != null) {
            showUserProfile(firebaseUser);
        } else {
        }






        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDrawer(drawerLayout);
            }
        });


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                redirectActivity(DashboardActivity.this, SettingsActivity.class);

            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                redirectActivity(DashboardActivity.this, AboutActivity.class);

            }
        });


        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               redirectActivity(DashboardActivity.this, Contacts.class);

            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               redirectActivity(DashboardActivity.this, PrivacyActivity.class);

            }
        });




        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(DashboardActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        });

        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emergencyNumber = "911";

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + emergencyNumber));

                if (ActivityCompat.checkSelfPermission(DashboardActivity.this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    ActivityCompat.requestPermissions(DashboardActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
            }
        });

        findmyparking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                redirectActivity(DashboardActivity.this, FindMyParkingActivity.class);

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
                    name = firebaseUser.getDisplayName();
                    Email = firebaseUser.getEmail();

                    Name.setText(name);
                    email.setText(Email);

                     if (modelUsers.getImage() != null && !modelUsers.getImage().isEmpty()) {
                        String imageUrl = modelUsers.getImage();
                        Picasso.get().load(imageUrl).resize(200, 200).centerCrop().into(dp);
                    } else {
                        dp.setImageResource(R.drawable.ic_default_img_white);
                        dp.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(DashboardActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public static void updateToken(String token) {
        if (mUID != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
            Token mToken = new Token(token);
            ref.child(mUID).setValue(mToken);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            actionBar.setTitle("Home");
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();

                            return true;
                        case R.id.nav_profile:
                            actionBar.setTitle("Profile");
                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            actionBar.setTitle("Users");
                            UsersFragment fragment3 = new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;
                        case R.id.nav_chat:
                            actionBar.setTitle("Chats");
                            ChatListFragment fragment4 = new ChatListFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, fragment4, "");
                            ft4.commit();
                            return true;


                        case R.id.nav_more:
                            shorMoreOptions();
                            return true;
                    }
                    return false;
                }
            };

    private void shorMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, navigationView, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Notifications");
        popupMenu.getMenu().add(Menu.NONE,1,0,"Group Chats");
        popupMenu.getMenu().add(Menu.NONE,2,0,"Rides");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){

                    actionBar.setTitle("Notifications");
                    NotificationsFragment fragment6 = new NotificationsFragment();
                    FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                    ft6.replace(R.id.content, fragment6, "");
                    ft6.commit();
                }
                else if (id == 1){

                    actionBar.setTitle("Group Chats");
                    GroupChatsFragment fragment7 = new GroupChatsFragment();
                    FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                    ft7.replace(R.id.content, fragment7, "");
                    ft7.commit();
                }
                else if(id == 2){
                    actionBar.setTitle("Rides");
                    RidesFragment fragment8 = new RidesFragment();
                    FragmentTransaction ft8 = getSupportFragmentManager().beginTransaction();
                    ft8.replace(R.id.content, fragment8, "");
                    ft8.commit();
                }
                return false;
            }
        });
        popupMenu.show();
    }


    public static void openDrawer (DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //mProfileTv.setText(user.getEmail());
            mUID = user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                String token = task.getResult();
                                updateToken(token);
                            }
                        }
                    });
        }
        else {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // Check if the navigation drawer is open, if it is, close it
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer(drawerLayout);
        } else {
            // If the navigation drawer is not open, do nothing (stay on the DashboardActivity)
        }
    }


    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }



}