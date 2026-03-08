package com.offlineai.app.models;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;
    public static final int TYPE_IMAGE = 2;

    private String text;
    private int type;
    private long timestamp;
    private String imagePath;

    public ChatMessage(String text, int type) {
        this.text = text;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String text, int type, String imagePath) {
        this(text, type);
        this.imagePath = imagePath;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public String getImagePath() { return imagePath; }
    public boolean hasImage() { return imagePath != null && !imagePath.isEmpty(); }
}
