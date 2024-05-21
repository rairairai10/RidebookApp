package com.example.ridebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.util.Calendar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import android.view.View;


import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


public class SetARideActivity extends AppCompatActivity  {

    private Button datebutton;
    private Button timebutton;
    Spinner spinner, spinner1;
    private int year, month, day, hour ,minute;
    private TextView meetingplace;

    private EditText meeting;

    FirebaseAuth firebaseAuth;

    String name, email, uid, dp;

    DatabaseReference userDbRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Set a Ride");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_set_aride);

        datebutton = findViewById(R.id.selectDate);
        timebutton = findViewById(R.id.SelectTime);
        Button setbutton = findViewById(R.id.set);
        spinner = findViewById(R.id.places_spinner);
        spinner1 = findViewById(R.id.places_spinner2);
        meetingplace = findViewById(R.id.textview_meetingplace);
        meeting = findViewById(R.id.edit_place);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();


        if (firebaseAuth.getCurrentUser() != null) {
            email = firebaseAuth.getCurrentUser().getEmail();
            Log.d("Debug", "User email: " + email); // Add this line to debug
            userDbRef = FirebaseDatabase.getInstance().getReference("Users");
            Query query = userDbRef.orderByChild("email").equalTo(email);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds: snapshot.getChildren()){
                        name = ""+ ds.child("name").getValue();
                        email = ""+ ds.child("email").getValue();
                        dp = ""+ ds.child("image").getValue();
                        Log.d("Debug", "Retrieved user info - Name: " + name + ", Email: " + email); // Add this line to debug
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.places_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> Adapter = ArrayAdapter.createFromResource(this, R.array.Category, android.R.layout.simple_spinner_item);
        Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);



        setbutton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View v) {
                final String meet = meeting.getText().toString();
                final String selectedPlace = spinner.getSelectedItem().toString();
                final String date = day + "/" + (month + 1) + "/" + year;
                @SuppressLint("DefaultLocale")
                final String time;
                if (hour >= 12) {
                    time = String.format("%02d:%02d PM", hour - 12, minute);
                } else {
                    time = String.format("%02d:%02d AM", hour, minute);
                }

                final String selectedCC = spinner1.getSelectedItem().toString();

                DatabaseReference ridesRef = FirebaseDatabase.getInstance().getReference("Rides");
                Query query = ridesRef.orderByChild("date_time").equalTo(date + " " + time);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // A ride with the same date and time already exists, show an error message
                            Toast.makeText(getApplicationContext(), "Ride with the same date and time already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            // No ride with the same date and time, proceed to add the new ride
                            DatabaseReference newRideRef = ridesRef.push();
                            newRideRef.child("destination").setValue(selectedPlace);
                            newRideRef.child("date").setValue(date);
                            newRideRef.child("time").setValue(time);
                            newRideRef.child("CC").setValue(selectedCC);
                            newRideRef.child("uid").setValue(uid);
                            newRideRef.child("uName").setValue(name);
                            newRideRef.child("uEmail").setValue(email);
                            newRideRef.child("uDp").setValue(dp);
                            newRideRef.child("meeting_place").setValue(meet);

                            newRideRef.child("date_time").setValue(date + " " + time);

                            newRideRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // After adding the ride, show success message and navigate to the dashboard
                                    Toast.makeText(getApplicationContext(), "Ride data set successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SetARideActivity.this, DashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle any errors while adding the ride
                                    Toast.makeText(getApplicationContext(), "Failed to set ride data", Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Reset input fields
                            meeting.setText("");
                            spinner.setSelection(0);
                            datebutton.setText(day + "/" + (month + 1) + "/" + year);
                            timebutton.setText(String.format("%02d:%02d", hour, minute));
                            spinner1.setSelection(0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any errors in the database query
                        Toast.makeText(getApplicationContext(), "Failed to check ride data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        datebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePicker();
            }
        });

        timebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
    }
    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(SetARideActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SetARideActivity.this.hour = hourOfDay;
                        SetARideActivity.this.minute = minute;

                        String amPm;
                        if (hourOfDay < 12) {
                            amPm = "AM";
                        } else {
                            amPm = "PM";
                            // Convert to 12-hour format if it's PM
                            if (hourOfDay > 12) {
                                hourOfDay -= 12;
                            }
                        }

                        // Update the time button text
                        timebutton.setText(String.format("%02d:%02d %s", hourOfDay, minute, amPm));
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }


    private void showDatePicker() {

        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(SetARideActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        SetARideActivity.this.year = year;
                        SetARideActivity.this.month = month;
                        SetARideActivity.this.day = dayOfMonth;

                        datebutton.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }


    private void checkUserStatus() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //mProfileTv.setText(user.getEmail());
        } else {
            startActivity(new Intent(SetARideActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_set).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}