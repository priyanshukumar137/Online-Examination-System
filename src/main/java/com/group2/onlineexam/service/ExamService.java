package com.group2.onlineexam.service;

import com.group2.onlineexam.model.Attempt;
import com.group2.onlineexam.model.Question;
import com.group2.onlineexam.model.User;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ExamService {
    private static final Path DATA_FILE = Path.of("data", "online-exam.dat");
    private final List<User> users = new ArrayList<>();
    private final List<Question> questions = new ArrayList<>();
    private final List<Attempt> attempts = new ArrayList<>();

    public ExamService() {
        load();
        if (users.isEmpty()) users.add(new User("Administrator", "admin", "admin123", User.Role.ADMIN));
        if (questions.isEmpty()) seedQuestions();
        save();
    }

    public boolean registerStudent(String name, String username, String password) {
        if (name == null || name.isBlank() || username == null || username.isBlank()
                || password == null || password.length() < 4 || findUser(username).isPresent()) return false;
        users.add(new User(name.trim(), username.trim(), password, User.Role.STUDENT));
        save(); return true;
    }

    public Optional<User> login(String username, String password) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username.trim()))
                .filter(u -> u.passwordMatches(password)).findFirst();
    }
    public void addQuestion(Question question) { questions.add(question); save(); }
    public List<Question> getQuestions() { return List.copyOf(questions); }
    public List<Attempt> getAttempts() { return List.copyOf(attempts); }
    public List<Attempt> getAttemptsFor(String username) {
        return attempts.stream().filter(a -> a.getUsername().equalsIgnoreCase(username)).toList();
    }
    public Attempt submitExam(String username, List<Integer> answers) {
        int score = 0;
        for (int i = 0; i < questions.size() && i < answers.size(); i++)
            if (questions.get(i).isCorrect(answers.get(i))) score++;
        Attempt attempt = new Attempt(username, score, questions.size());
        attempts.add(attempt); save(); return attempt;
    }
    private Optional<User> findUser(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username.trim())).findFirst();
    }
    private void seedQuestions() {
        questions.add(new Question("Which keyword is used to inherit a class in Java?", List.of("implements","extends","inherits","super"), 1));
        questions.add(new Question("Which method is the entry point of a Java application?", List.of("start()","run()","main()","init()"), 2));
        questions.add(new Question("Which data type stores true or false?", List.of("int","String","char","boolean"), 3));
        questions.add(new Question("Which collection does not allow duplicate elements?", List.of("ArrayList","HashSet","LinkedList","Vector"), 1));
        questions.add(new Question("What is the size of an int in Java?", List.of("8 bits","16 bits","32 bits","64 bits"), 2));
    }
    private void load() {
        if (!Files.exists(DATA_FILE)) return;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(DATA_FILE))) {
            Object data = in.readObject();
            if (data instanceof SavedData saved) {
                users.addAll(saved.users); questions.addAll(saved.questions); attempts.addAll(saved.attempts);
            }
        } catch (IOException | ClassNotFoundException | RuntimeException ex) {
            System.out.println("Saved data could not be loaded; starting with fresh sample data.");
        }
    }
    private void save() {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(DATA_FILE))) {
                out.writeObject(new SavedData(users, questions, attempts));
            }
        } catch (IOException ex) {
            System.out.println("Warning: data could not be saved: " + ex.getMessage());
        }
    }
    private static class SavedData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final List<User> users; private final List<Question> questions; private final List<Attempt> attempts;
        private SavedData(List<User> u, List<Question> q, List<Attempt> a) {
            users = new ArrayList<>(u); questions = new ArrayList<>(q); attempts = new ArrayList<>(a);
        }
    }
}