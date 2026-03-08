package com.offlineai.app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResponseFormatter {

    // Add typing simulation delay in ms based on answer length
    public static long getTypingDelay(String response) {
        int words = response.split("\\s+").length;
        long delay = Math.min(words * 30L, 2000L); // max 2 seconds
        return Math.max(delay, 400L); // min 400ms
    }

    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String generateSessionTitle(String firstMessage) {
        if (firstMessage == null || firstMessage.isEmpty()) return "New Chat";
        String trimmed = firstMessage.trim();
        if (trimmed.length() <= 35) return trimmed;
        return trimmed.substring(0, 32) + "...";
    }
}
