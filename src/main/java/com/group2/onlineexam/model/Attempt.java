package com.group2.onlineexam.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Attempt implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String username;
    private final int score;
    private final int total;
    private final LocalDateTime submittedAt;

    public Attempt(String username, int score, int total) {
        this.username = username; this.score = score; this.total = total; this.submittedAt = LocalDateTime.now();
    }
    public String getUsername() { return username; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public double getPercentage() { return total == 0 ? 0 : score * 100.0 / total; }
    public boolean isPassed() { return getPercentage() >= 40.0; }
    public String getSubmittedAt() { return submittedAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")); }
}