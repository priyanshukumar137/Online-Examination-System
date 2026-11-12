package com.group2.onlineexam;

import com.group2.onlineexam.model.Attempt;
import com.group2.onlineexam.model.Question;
import com.group2.onlineexam.model.User;
import com.group2.onlineexam.service.ExamService;
import java.util.*;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final ExamService SERVICE = new ExamService();

    public static void main(String[] args) {
        System.out.println("\n======================================");
        System.out.println("       ONLINE EXAMINATION SYSTEM");
        System.out.println("======================================");
        while (true) {
            System.out.println("\n1. Student Registration\n2. Login\n3. Exit");
            switch (readInt("Choose an option: ", 1, 3)) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> { System.out.println("Thank you for using the system!"); return; }
                default -> { }
            }
        }
    }
    private static void register() {
        System.out.println("\n--- Student Registration ---");
        String name = readText("Full name: ");
        String username = readText("Choose username: ");
        String password = readText("Choose password (minimum 4 characters): ");
        System.out.println(SERVICE.registerStudent(name, username, password)
                ? "Registration successful. You can now log in."
                : "Registration failed. Username may exist or details are invalid.");
    }
    private static void login() {
        System.out.println("\n--- Login ---");
        String username = readText("Username: ");
        String password = readText("Password: ");
        Optional<User> found = SERVICE.login(username, password);
        if (found.isEmpty()) { System.out.println("Invalid username or password."); return; }
        User user = found.get();
        System.out.println("Welcome, " + user.getName() + "!");
        if (user.getRole() == User.Role.ADMIN) adminMenu(); else studentMenu(user);
    }
    private static void adminMenu() {
        while (true) {
            System.out.println("\n--- Administrator Dashboard ---");
            System.out.println("1. Add MCQ question\n2. View question bank\n3. View all results\n4. Logout");
            switch (readInt("Choose an option: ", 1, 4)) {
                case 1 -> addQuestion();
                case 2 -> showQuestions();
                case 3 -> showAttempts(SERVICE.getAttempts());
                case 4 -> { return; }
                default -> { }
            }
        }
    }
    private static void studentMenu(User user) {
        while (true) {
            System.out.println("\n--- Student Dashboard ---");
            System.out.println("1. View examination\n2. Take examination\n3. View my results\n4. Logout");
            switch (readInt("Choose an option: ", 1, 4)) {
                case 1 -> System.out.println("Java MCQ Examination | Questions: " + SERVICE.getQuestions().size()
                        + " | 1 mark each | Pass mark: 40%");
                case 2 -> takeExam(user);
                case 3 -> showAttempts(SERVICE.getAttemptsFor(user.getUsername()));
                case 4 -> { return; }
                default -> { }
            }
        }
    }
    private static void addQuestion() {
        System.out.println("\n--- Add New Question ---");
        String text = readText("Question: ");
        List<String> options = new ArrayList<>();
        for (int i = 0; i < 4; i++) options.add(readText("Option " + (char)('A' + i) + ": "));
        int correct = readInt("Correct option (1=A, 2=B, 3=C, 4=D): ", 1, 4) - 1;
        try { SERVICE.addQuestion(new Question(text, options, correct)); System.out.println("Question added."); }
        catch (IllegalArgumentException ex) { System.out.println("Could not add question: " + ex.getMessage()); }
    }
    private static void showQuestions() {
        List<Question> qs = SERVICE.getQuestions();
        System.out.println("\n--- Question Bank (" + qs.size() + ") ---");
        for (int i = 0; i < qs.size(); i++) {
            Question q = qs.get(i); System.out.println((i + 1) + ". " + q.getText());
            for (int j = 0; j < 4; j++) System.out.println("   " + (char)('A' + j) + ". " + q.getOptions().get(j));
        }
    }
    private static void takeExam(User user) {
        List<Question> qs = SERVICE.getQuestions();
        if (qs.isEmpty()) { System.out.println("No questions available."); return; }
        System.out.println("\n========== EXAMINATION ==========\nEnter A, B, C or D for each question.");
        List<Integer> answers = new ArrayList<>();
        for (int i = 0; i < qs.size(); i++) {
            Question q = qs.get(i); System.out.println("\nQ" + (i + 1) + ". " + q.getText());
            for (int j = 0; j < 4; j++) System.out.println((char)('A' + j) + ". " + q.getOptions().get(j));
            String answer;
            do {
                answer = readText("Your answer (A/B/C/D): ").toUpperCase(Locale.ROOT);
                if (!answer.matches("[ABCD]")) System.out.println("Please enter A, B, C or D.");
            } while (!answer.matches("[ABCD]"));
            answers.add(answer.charAt(0) - 'A');
        }
        Attempt result = SERVICE.submitExam(user.getUsername(), answers);
        System.out.println("\n========== RESULT ==========");
        System.out.printf("Score: %d/%d%nPercentage: %.2f%%%nStatus: %s%n", result.getScore(),
                result.getTotal(), result.getPercentage(), result.isPassed() ? "PASS" : "FAIL");
    }
    private static void showAttempts(List<Attempt> attempts) {
        System.out.println("\n--- Examination Results ---");
        if (attempts.isEmpty()) { System.out.println("No examination attempts found."); return; }
        System.out.printf("%-18s %-10s %-12s %-8s %-18s%n", "Username", "Score", "Percentage", "Status", "Submitted at");
        for (Attempt a : attempts) System.out.printf("%-18s %-10s %-11.2f%% %-8s %-18s%n",
                a.getUsername(), a.getScore() + "/" + a.getTotal(), a.getPercentage(),
                a.isPassed() ? "PASS" : "FAIL", a.getSubmittedAt());
    }
    private static String readText(String prompt) { System.out.print(prompt); return SCANNER.nextLine().trim(); }
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readText(prompt);
            try { int n = Integer.parseInt(value); if (n >= min && n <= max) return n; }
            catch (NumberFormatException ignored) { }
            System.out.println("Enter a number between " + min + " and " + max + ".");
        }
    }
}