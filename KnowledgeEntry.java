package com.offlineai.app.models;

import java.util.List;

public class KnowledgeEntry {
    private String id;
    private List<String> keywords;
    private String answer;
    private String category;
    private int relevanceScore;

    public KnowledgeEntry() {}

    public KnowledgeEntry(String id, List<String> keywords, String answer, String category) {
        this.id = id;
        this.keywords = keywords;
        this.answer = answer;
        this.category = category;
    }

    public String getId() { return id; }
    public List<String> getKeywords() { return keywords; }
    public String getAnswer() { return answer; }
    public String getCategory() { return category; }
    public int getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(int score) { this.relevanceScore = score; }
}
