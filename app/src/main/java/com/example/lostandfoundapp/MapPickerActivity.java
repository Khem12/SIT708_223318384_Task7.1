package com.example.lostandfoundapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng selectedLocation;
    private Button btnConfirmLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        // Get any existing location from intent
        double lat = getIntent().getDoubleExtra("latitude", -37.8136); // Default to Sydney
        double lng = getIntent().getDoubleExtra("longitude", 144.9631);
        selectedLocation = new LatLng(lat, lng);

        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        btnConfirmLocation.setOnClickListener(v -> confirmLocation());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add marker at initial position
        mMap.addMarker(new MarkerOptions().position(selectedLocation).title("Selected Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));

        // Set click listener for map
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        });
    }

    private void confirmLocation() {
        if (selectedLocation != null) {
            // Try to get address from coordinates
            String address = getAddressFromLocation(selectedLocation);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", selectedLocation.latitude);
            resultIntent.putExtra("longitude", selectedLocation.longitude);
            resultIntent.putExtra("address", address);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}