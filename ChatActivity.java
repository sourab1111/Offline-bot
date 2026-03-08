package com.offlineai.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineai.app.R;
import com.offlineai.app.adapters.ChatAdapter;
import com.offlineai.app.database.DatabaseHelper;
import com.offlineai.app.engine.SearchEngine;
import com.offlineai.app.models.ChatMessage;
import com.offlineai.app.utils.ResponseFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_VOICE = 100;
    private static final int REQUEST_AUDIO_PERMISSION = 101;

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private EditText etInput;
    private ImageButton btnSend, btnVoice;
    private View typingIndicator;

    private DatabaseHelper db;
    private SearchEngine engine;
    private long sessionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("AI Assistant");

        db = DatabaseHelper.getInstance(this);
        engine = SearchEngine.getInstance(this);

        recyclerView = findViewById(R.id.recycler_chat);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);
        btnVoice = findViewById(R.id.btn_voice);
        typingIndicator = findViewById(R.id.typing_indicator);

        chatAdapter = new ChatAdapter(messages, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        // Load existing session if provided
        if (getIntent().hasExtra("session_id")) {
            sessionId = getIntent().getLongExtra("session_id", -1);
            loadSession();
        } else {
            // Show welcome message
            addAiMessage("Hello! I'm your offline AI assistant 🤖\n\nI can answer questions about science, history, health, technology, business, and much more.\n\nType or speak your question!");
        }

        btnSend.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (!text.isEmpty()) {
                etInput.setText("");
                sendMessage(text);
            }
        });

        btnVoice.setOnClickListener(v -> startVoiceInput());

        // Check session from intent
        if (getIntent().hasExtra("category_prompt")) {
            String prompt = getIntent().getStringExtra("category_prompt");
            sendMessage(prompt);
        }
    }

    private void loadSession() {
        List<ChatMessage> saved = db.getSessionMessages(sessionId);
        if (!saved.isEmpty()) {
            messages.addAll(saved);
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messages.size() - 1);
            String[] info = db.getSessionInfo(sessionId);
            getSupportActionBar().setTitle(info[0]);
        }
    }

    private void sendMessage(String text) {
        // Add user message
        ChatMessage userMsg = new ChatMessage(text, ChatMessage.TYPE_USER);
        messages.add(userMsg);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);

        // Create session if first message
        if (sessionId == -1) {
            sessionId = db.createSession(ResponseFormatter.generateSessionTitle(text), text);
        }
        db.saveMessage(sessionId, userMsg);

        // Show typing indicator
        typingIndicator.setVisibility(View.VISIBLE);

        // Search in background
        new Thread(() -> {
            String answer = engine.search(text);
            long delay = ResponseFormatter.getTypingDelay(answer);

            try { Thread.sleep(delay); } catch (InterruptedException ignored) {}

            runOnUiThread(() -> {
                typingIndicator.setVisibility(View.GONE);
                addAiMessage(answer);
                db.updateSession(sessionId, answer.length() > 60 ? answer.substring(0, 57) + "..." : answer);
            });
        }).start();
    }

    private void addAiMessage(String text) {
        ChatMessage aiMsg = new ChatMessage(text, ChatMessage.TYPE_AI);
        messages.add(aiMsg);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
        if (sessionId != -1) {
            db.saveMessage(sessionId, aiMsg);
        }
    }

    private void startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your question...");

        try {
            startActivityForResult(intent, REQUEST_VOICE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOICE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                etInput.setText(spokenText);
                sendMessage(spokenText);
                etInput.setText("");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceInput();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
