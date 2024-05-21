package com.example.ridebook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import adapters.AdapterRides;
import models.ModelRides;

public class RidesActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelRides> postRides;
    AdapterRides adapterRides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides);

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.ridesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(layoutManager);
        postRides = new ArrayList<>();
        adapterRides = new AdapterRides(this, postRides);
        recyclerView.setAdapter(adapterRides);

        loadRides();
    }

    private void loadRides() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Rides");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postRides.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelRides modelRides = ds.getValue(ModelRides.class);
                    postRides.add(modelRides);
                }
                adapterRides.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RidesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RidesActivity.this,DashboardActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
