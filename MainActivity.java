package com.offlineai.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.offlineai.app.R;
import com.offlineai.app.engine.SearchEngine;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Stats
        TextView tvStats = findViewById(R.id.tv_stats);
        new Thread(() -> {
            try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            runOnUiThread(() -> {
                SearchEngine engine = SearchEngine.getInstance(this);
                tvStats.setText(engine.getKnowledgeCount() + " knowledge entries loaded");
            });
        }).start();

        // Chat button
        CardView cardChat = findViewById(R.id.card_chat);
        cardChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

        // History button
        CardView cardHistory = findViewById(R.id.card_history);
        cardHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

        // Categories button
        CardView cardCategories = findViewById(R.id.card_categories);
        cardCategories.setOnClickListener(v -> startActivity(new Intent(this, CategoriesActivity.class)));

        // Image Analysis button
        CardView cardImage = findViewById(R.id.card_image);
        cardImage.setOnClickListener(v -> startActivity(new Intent(this, ImageAnalysisActivity.class)));
    }
}
