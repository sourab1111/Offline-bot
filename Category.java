package com.offlineai.app.models;

public class Category {
    private String id;
    private String name;
    private String icon;
    private String description;
    private int color;

    public Category(String id, String name, String icon, String description, int color) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.color = color;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    public int getColor() { return color; }
}
