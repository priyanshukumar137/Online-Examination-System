package com.group2.onlineexam.model;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String text;
    private final List<String> options;
    private final int correctOption;

    public Question(String text, List<String> options, int correctOption) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Question cannot be empty.");
        if (options == null || options.size() != 4) throw new IllegalArgumentException("Exactly four options are required.");
        if (correctOption < 0 || correctOption > 3) throw new IllegalArgumentException("Correct option must be A, B, C or D.");
        this.text = text; this.options = List.copyOf(options); this.correctOption = correctOption;
    }
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public boolean isCorrect(int selectedOption) { return selectedOption == correctOption; }
}