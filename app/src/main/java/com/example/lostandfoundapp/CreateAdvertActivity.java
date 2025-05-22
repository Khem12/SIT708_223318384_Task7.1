package com.example.lostandfoundapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.example.lostandfoundapp.MapPickerActivity;

import java.util.Arrays;
import java.util.List;

public class CreateAdvertActivity extends AppCompatActivity {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 100;
    private static final int MAP_LOCATION_REQUEST_CODE = 102;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    private Spinner typeSpinner;
    private EditText nameEditText, phoneEditText, descriptionEditText, dateEditText, locationEditText;
    private Button saveButton, btnCurrentLocation, btnShowOnMap;

    private LatLng selectedLatLng;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        // Initialize Google Places
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBCwwSJP8JOsIUji_o3SAAs_rfPxZMK_WY"); // Replace with actual API key
        }

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI elements
        typeSpinner = findViewById(R.id.typeSpinner);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateEditText = findViewById(R.id.dateEditText);
        locationEditText = findViewById(R.id.locationEditText);
        saveButton = findViewById(R.id.saveButton);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnShowOnMap = findViewById(R.id.btnShowOnMap);

        // Setup spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.post_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        // Autocomplete picker
        locationEditText.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(CreateAdvertActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        // Current location button
        btnCurrentLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    locationEditText.setText("Lat: " + selectedLatLng.latitude + ", Lng: " + selectedLatLng.longitude);
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Show on map button
        btnShowOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAdvertActivity.this, MapPickerActivity.class);
            if (selectedLatLng != null) {
                intent.putExtra("latitude", selectedLatLng.latitude);
                intent.putExtra("longitude", selectedLatLng.longitude);
            }
            startActivityForResult(intent, MAP_LOCATION_REQUEST_CODE);
        });

        // Save advert
        saveButton.setOnClickListener(v -> saveAdvert());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            selectedLatLng = place.getLatLng();
            locationEditText.setText(place.getName());
        } else if (requestCode == MAP_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            String address = data.getStringExtra("address");

            selectedLatLng = new LatLng(latitude, longitude);
            locationEditText.setText(address != null ? address : "Lat: " + latitude + ", Lng: " + longitude);
        }
    }

    private void saveAdvert() {
        String type = typeSpinner.getSelectedItem().toString();
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || description.isEmpty() ||
                date.isEmpty() || location.isEmpty() || selectedLatLng == null) {
            Toast.makeText(this, "Please fill all fields and select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isInserted = dbHelper.addItem(
                type, name, phone, description, date, location,
                selectedLatLng.latitude, selectedLatLng.longitude
        );

        if (isInserted) {
            Toast.makeText(this, "Advert saved successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CreateAdvertActivity.this, ShowItemsActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Failed to save advert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnCurrentLocation.performClick(); // Retry getting current location
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
