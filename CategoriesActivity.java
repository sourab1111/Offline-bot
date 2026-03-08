package com.offlineai.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineai.app.R;
import com.offlineai.app.adapters.CategoryAdapter;
import com.offlineai.app.models.Category;

import java.util.Arrays;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Knowledge Categories");

        List<Category> categories = Arrays.asList(
                new Category("science", "Science", "🔬", "Physics, chemistry, biology and more", 0xFF1565C0),
                new Category("history", "History", "📜", "World events and civilizations", 0xFF6A1B9A),
                new Category("technology", "Technology", "💻", "Computers, AI, software & hardware", 0xFF00695C),
                new Category("health", "Health & Medicine", "🏥", "Body, wellness and medical info", 0xFFC62828),
                new Category("mathematics", "Mathematics", "📐", "Numbers, equations and formulas", 0xFF558B2F),
                new Category("business", "Business", "💼", "Finance, entrepreneurship & economics", 0xFFEF6C00),
                new Category("geography", "Geography", "🌍", "Countries, capitals and natural world", 0xFF00838F),
                new Category("language", "Language & Grammar", "📝", "Writing, grammar and communication", 0xFF4527A0),
                new Category("general", "General Knowledge", "💡", "Everything else you want to know", 0xFF37474F)
        );

        RecyclerView recyclerView = findViewById(R.id.recycler_categories);
        CategoryAdapter adapter = new CategoryAdapter(categories, this, category -> {
            Intent intent = new Intent(CategoriesActivity.this, ChatActivity.class);
            intent.putExtra("category_prompt", "Tell me about " + category.getName());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
