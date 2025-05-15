package com.example.lostandfoundapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {
    private TextView typeTextView, nameTextView, phoneTextView, descriptionTextView, dateTextView, locationTextView;
    private Button deleteButton;
    private DatabaseHelper dbHelper;
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        dbHelper = new DatabaseHelper(this);
        itemId = getIntent().getStringExtra("id");

        typeTextView = findViewById(R.id.typeDetailTextView);
        nameTextView = findViewById(R.id.nameDetailTextView);
        phoneTextView = findViewById(R.id.phoneDetailTextView);
        descriptionTextView = findViewById(R.id.descriptionDetailTextView);
        dateTextView = findViewById(R.id.dateDetailTextView);
        locationTextView = findViewById(R.id.locationDetailTextView);
        deleteButton = findViewById(R.id.deleteButton);

        loadItemDetails();

        deleteButton.setOnClickListener(v -> deleteItem());
    }

    private void loadItemDetails() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ITEMS,
                null,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{itemId},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // Safely get column indices
            int typeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
            int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE);
            int descIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION);
            int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE);
            int locationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION);

            // Set text only if column exists (index != -1)
            if (typeIndex != -1) typeTextView.setText(cursor.getString(typeIndex));
            if (nameIndex != -1) nameTextView.setText(cursor.getString(nameIndex));
            if (phoneIndex != -1) phoneTextView.setText(cursor.getString(phoneIndex));
            if (descIndex != -1) descriptionTextView.setText(cursor.getString(descIndex));
            if (dateIndex != -1) dateTextView.setText(cursor.getString(dateIndex));
            if (locationIndex != -1) locationTextView.setText(cursor.getString(locationIndex));

            cursor.close();
        } else {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void deleteItem() {
        boolean isDeleted = dbHelper.deleteItem(itemId);

        if (isDeleted) {
            Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
        }
    }
}