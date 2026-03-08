package com.offlineai.app.engine;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.offlineai.app.models.KnowledgeEntry;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchEngine {

    private static final String TAG = "SearchEngine";
    private static SearchEngine instance;
    private List<KnowledgeEntry> knowledgeBase = new ArrayList<>();
    private boolean isLoaded = false;

    // Common words to ignore in search
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "shall", "can", "to", "of", "in", "on",
            "at", "by", "for", "with", "about", "as", "into", "through", "it",
            "its", "this", "that", "these", "those", "i", "you", "he", "she",
            "we", "they", "what", "how", "why", "when", "where", "which", "who",
            "me", "my", "your", "his", "her", "our", "their", "and", "but", "or",
            "if", "then", "than", "so", "not", "no", "up", "out", "get"
    ));

    public static synchronized SearchEngine getInstance(Context context) {
        if (instance == null) {
            instance = new SearchEngine();
            instance.loadKnowledgeBase(context);
        }
        return instance;
    }

    private void loadKnowledgeBase(Context context) {
        new Thread(() -> {
            try {
                // Load all JSON files from assets/knowledge/
                String[] files = context.getAssets().list("knowledge");
                if (files == null) return;

                Gson gson = new Gson();
                Type listType = new TypeToken<List<KnowledgeEntry>>(){}.getType();

                for (String file : files) {
                    if (!file.endsWith(".json")) continue;
                    InputStream is = context.getAssets().open("knowledge/" + file);
                    byte[] buffer = new byte[is.available()];
                    is.read(buffer);
                    is.close();
                    String json = new String(buffer, StandardCharsets.UTF_8);
                    List<KnowledgeEntry> entries = gson.fromJson(json, listType);
                    if (entries != null) {
                        knowledgeBase.addAll(entries);
                    }
                }
                isLoaded = true;
                Log.d(TAG, "Knowledge base loaded: " + knowledgeBase.size() + " entries");
            } catch (IOException e) {
                Log.e(TAG, "Error loading knowledge base", e);
            }
        }).start();
    }

    public String search(String userInput) {
        if (!isLoaded || knowledgeBase.isEmpty()) {
            return "I'm still loading my knowledge base. Please try again in a moment.";
        }

        List<String> inputKeywords = extractKeywords(userInput.toLowerCase());

        if (inputKeywords.isEmpty()) {
            return getFallbackResponse(userInput);
        }

        // Score all entries
        List<KnowledgeEntry> scored = new ArrayList<>();
        for (KnowledgeEntry entry : knowledgeBase) {
            int score = calculateScore(inputKeywords, entry);
            if (score > 0) {
                entry.setRelevanceScore(score);
                scored.add(entry);
            }
        }

        if (scored.isEmpty()) {
            return getFallbackResponse(userInput);
        }

        // Sort by relevance descending
        Collections.sort(scored, (a, b) -> b.getRelevanceScore() - a.getRelevanceScore());

        KnowledgeEntry best = scored.get(0);

        // If score is too low, return fallback
        if (best.getRelevanceScore() < 2) {
            return getFallbackResponse(userInput);
        }

        return best.getAnswer();
    }

    private int calculateScore(List<String> inputKeywords, KnowledgeEntry entry) {
        int score = 0;
        List<String> entryKeywords = entry.getKeywords();
        if (entryKeywords == null) return 0;

        for (String inputWord : inputKeywords) {
            for (String entryWord : entryKeywords) {
                String ew = entryWord.toLowerCase();
                if (ew.equals(inputWord)) {
                    score += 3; // exact match
                } else if (ew.contains(inputWord) || inputWord.contains(ew)) {
                    score += 1; // partial match
                }
            }
        }

        // Bonus: check if input phrase appears in keywords
        String inputFull = String.join(" ", inputKeywords);
        for (String entryWord : entryKeywords) {
            if (entryWord.toLowerCase().contains(inputFull)) {
                score += 5;
            }
        }

        return score;
    }

    public List<String> extractKeywords(String text) {
        String cleaned = text.replaceAll("[^a-z0-9\\s]", " ").trim();
        String[] words = cleaned.split("\\s+");
        List<String> keywords = new ArrayList<>();
        for (String word : words) {
            if (!word.isEmpty() && !STOP_WORDS.contains(word) && word.length() > 2) {
                keywords.add(word);
            }
        }
        return keywords;
    }

    private String getFallbackResponse(String input) {
        String lower = input.toLowerCase();
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello! I'm your offline AI assistant. Ask me anything — I'll search my knowledge base to help you!";
        }
        if (lower.contains("thank")) {
            return "You're welcome! Is there anything else I can help you with?";
        }
        if (lower.contains("who are you") || lower.contains("what are you")) {
            return "I'm an offline AI assistant. I work entirely without internet using a built-in knowledge base. Ask me about science, history, health, technology, business, and much more!";
        }
        return "I don't have specific information about that yet, but I'm always learning! Try rephrasing your question, or explore my Categories to see what topics I know about.";
    }

    public List<KnowledgeEntry> searchByCategory(String category) {
        List<KnowledgeEntry> results = new ArrayList<>();
        for (KnowledgeEntry entry : knowledgeBase) {
            if (category.equalsIgnoreCase(entry.getCategory())) {
                results.add(entry);
            }
        }
        return results;
    }

    public boolean isReady() {
        return isLoaded;
    }

    public int getKnowledgeCount() {
        return knowledgeBase.size();
    }
}
