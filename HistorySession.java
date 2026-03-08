package com.offlineai.app.models;

public class HistorySession {
    private long id;
    private String title;
    private String preview;
    private long timestamp;

    public HistorySession(long id, String title, String preview, long timestamp) {
        this.id = id;
        this.title = title;
        this.preview = preview;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getPreview() { return preview; }
    public long getTimestamp() { return timestamp; }
}
