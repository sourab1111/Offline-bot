package com.offlineai.app.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineai.app.R;
import com.offlineai.app.adapters.HistoryAdapter;
import com.offlineai.app.database.DatabaseHelper;
import com.offlineai.app.models.HistorySession;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistorySession> sessions = new ArrayList<>();
    private DatabaseHelper db;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chat History");

        db = DatabaseHelper.getInstance(this);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_history);

        adapter = new HistoryAdapter(sessions, this, new HistoryAdapter.OnSessionClickListener() {
            @Override
            public void onSessionClick(HistorySession session) {
                Intent intent = new Intent(HistoryActivity.this, ChatActivity.class);
                intent.putExtra("session_id", session.getId());
                startActivity(intent);
            }

            @Override
            public void onSessionDelete(HistorySession session) {
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle("Delete Chat")
                        .setMessage("Are you sure you want to delete this chat?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deleteSession(session.getId());
                            loadHistory();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        loadHistory();
    }

    private void loadHistory() {
        sessions.clear();
        Cursor cursor = db.getHistorySessions();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String title = cursor.getString(1);
            String preview = cursor.getString(2);
            long timestamp = cursor.getLong(3);
            sessions.add(new HistorySession(id, title, preview, timestamp));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(sessions.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
