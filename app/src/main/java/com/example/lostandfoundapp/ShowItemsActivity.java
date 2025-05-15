package com.example.lostandfoundapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ShowItemsActivity extends AppCompatActivity {
    private ListView itemsListView;
    private DatabaseHelper dbHelper;
    private SimpleCursorAdapter adapter;
    private Cursor currentCursor; // Track the current cursor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_items);

        dbHelper = new DatabaseHelper(this);
        itemsListView = findViewById(R.id.itemsListView);
        TextView emptyView = findViewById(R.id.emptyView);
        itemsListView.setEmptyView(emptyView);

        // Initialize with empty cursor
        currentCursor = dbHelper.getAllItems();
        setupAdapter(currentCursor);

        itemsListView.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) adapter.getItem(position);
            int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            if (idColumnIndex != -1) {
                Intent intent = new Intent(this, ItemDetailActivity.class);
                intent.putExtra("id", cursor.getString(idColumnIndex));
                startActivity(intent);
            }
        });
    }

    private void setupAdapter(Cursor cursor) {
        String[] fromColumns = {
                DatabaseHelper.COLUMN_TYPE,
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_DESCRIPTION
        };

        int[] toViews = {
                R.id.typeTextView,
                R.id.nameTextView,
                R.id.descriptionTextView
        };

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.item_list_row,
                cursor,
                fromColumns,
                toViews,
                0
        );
        itemsListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        // Close old cursor if exists
        if (currentCursor != null && !currentCursor.isClosed()) {
            currentCursor.close();
        }

        // Get new cursor
        currentCursor = dbHelper.getAllItems();
        adapter.changeCursor(currentCursor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up cursors
        if (currentCursor != null && !currentCursor.isClosed()) {
            currentCursor.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}