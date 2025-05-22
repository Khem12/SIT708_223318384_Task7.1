package com.example.lostandfoundapp;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.lostandfoundapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.database.Cursor;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = new DatabaseHelper(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get items from database
        Cursor cursor = dbHelper.getAllItems();
        if (cursor != null && cursor.moveToFirst()) {
            int typeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
            int latIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE);
            int lngIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE);

            do {
                String type = cursor.getString(typeIndex);
                String name = cursor.getString(nameIndex);
                double latitude = cursor.getDouble(latIndex);
                double longitude = cursor.getDouble(lngIndex);

                LatLng location = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(name)
                        .snippet(type));

                // Zoom to first item
                if (cursor.isFirst()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
                }
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}