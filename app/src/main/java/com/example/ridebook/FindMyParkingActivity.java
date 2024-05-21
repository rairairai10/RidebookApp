package com.example.ridebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class FindMyParkingActivity extends AppCompatActivity {

    Spinner spinner;
    Button btFind;
    SupportMapFragment supportMapFragment;
    static GoogleMap map;

    AutoCompleteTextView autoCompleteTextView;


    FusedLocationProviderClient fusedLocationProviderClient;


    static double currentLat = 0;
    static double currentLong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Locate & Explore");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        setContentView(R.layout.activity_find_my_parking);

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        btFind = findViewById(R.id.bt_find);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(FindMyParkingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();

        } else {
            ActivityCompat.requestPermissions(FindMyParkingActivity.this
                    , new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new String[]{});
        autoCompleteTextView.setAdapter(adapter);

        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String placeName = autoCompleteTextView.getText().toString().trim();
                if (!placeName.isEmpty()) {
                    String url = "https://maps.googleapis.com/maps/api/place/textsearch/json" +
                            "?query=" + placeName +
                            "&location=" + currentLat + "," + currentLong +
                            "&radius=200" +
                            "&key=" + getResources().getString(R.string.google_map_key) +
                            "&availability=free";

                    new PlaceTask().execute(url);
                }
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {

                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            map = googleMap;

                            LatLng currentLatLng = new LatLng(currentLat, currentLong);
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(currentLatLng)
                                    .title("Current Location");

                            map.addMarker(markerOptions);
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();

            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class PlaceTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            try {
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        URL url = new URL(string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String data = builder.toString();

        reader.close();
        return data;
    }

    private static class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JsonParser jsonParser = new JsonParser();

            List<HashMap<String, String>> mapList = null;

            try {
                JSONObject object = new JSONObject(strings[0]);
                mapList = jsonParser.parseResult(object);
            } catch (JSONException e) {
                // Handle the JSON parsing exception gracefully, e.g., by logging it.
                e.printStackTrace(); // Print the exception trace to logcat.
            }

            return mapList;
        }


        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            super.onPostExecute(hashMaps);
            if (map != null) {
                map.clear();

                LatLng currentLatLng = new LatLng(currentLat, currentLong);
                MarkerOptions currentMarkerOptions = new MarkerOptions()
                        .position(currentLatLng)
                        .title("Current Location");
                map.addMarker(currentMarkerOptions);


                for (HashMap<String, String> hashMapList : hashMaps) {
                    String latString = hashMapList.get("lat");
                    String lngString = hashMapList.get("long");
                    String name = hashMapList.get("name");

                    if (latString != null && !latString.isEmpty()
                            && lngString != null && !lngString.isEmpty()) {
                        double lat = Double.parseDouble(latString);
                        double lng = Double.parseDouble(lngString);
                        LatLng latLng = new LatLng(lat, lng);
                        MarkerOptions options = new MarkerOptions();
                        options.position(latLng);
                        options.title(name);
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.fmp_logo));
                        map.addMarker(options);
                    }
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}