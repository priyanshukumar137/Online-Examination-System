package com.group2.onlineexam.ui;

import com.group2.onlineexam.model.Attempt;
import com.group2.onlineexam.model.Question;
import com.group2.onlineexam.model.User;
import com.group2.onlineexam.service.ExamService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Desktop interface for the Online Examination System.
 * Uses the existing ExamService, so student accounts, questions and results
 * continue to use the same local persistence layer.
 */
public class OnlineExamApp {
    private static final Color NAVY = new Color(24, 40, 78);
    private static final Color BLUE = new Color(49, 105, 210);
    private static final Color BG = new Color(245, 247, 251);
    private static final Color TEXT = new Color(38, 48, 65);
    private static final Color MUTED = new Color(112, 123, 142);
    private static final Color GREEN = new Color(32, 150, 105);

    private final ExamService service = new ExamService();
    private final JFrame frame = new JFrame("Online Examination System");
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private User currentUser;
    private List<Question> examQuestions = new ArrayList<>();
    private List<Integer> answers = new ArrayList<>();
    private int questionIndex;
    private int secondsLeft;
    private Timer examTimer;
    private JLabel timerLabel;
    private JLabel questionCounter;
    private JLabel questionText;
    private final JRadioButton[] optionButtons = new JRadioButton[4];
    private final ButtonGroup optionGroup = new ButtonGroup();

    public OnlineExamApp() {
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 13));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(960, 650));
        frame.setSize(1120, 740);
        frame.setLocationRelativeTo(null);
        root.add(buildLogin(), "login");
        root.add(buildRegister(), "register");
        frame.setContentPane(root);
    }

    public void show() {
        frame.setVisible(true);
        cards.show(root, "login");
    }

    private JPanel buildLogin() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BG);
        JPanel card = formCard();
        JLabel brand = heading("EXAMFLOW", 27, NAVY);
        JLabel title = heading("Welcome back", 24, TEXT);
        JLabel subtitle = label("Sign in to continue to your examination portal", 13, MUTED);
        JTextField username = inputField();
        JPasswordField password = passwordField();
        JButton signIn = primaryButton("Sign In");
        JButton register = linkButton("Create student account");
        signIn.addActionListener(e -> {
            var found = service.login(username.getText().trim(), new String(password.getPassword()));
            if (found.isEmpty()) {
                showError("Invalid username or password.");
                return;
            }
            currentUser = found.get();
            showDashboard();
        });
        register.addActionListener(e -> cards.show(root, "register"));
        card.add(brand); card.add(Box.createVerticalStrut(22)); card.add(title);
        card.add(Box.createVerticalStrut(5)); card.add(subtitle);
        card.add(Box.createVerticalStrut(24)); card.add(fieldLabel("USERNAME"));
        card.add(username); card.add(Box.createVerticalStrut(13));
        card.add(fieldLabel("PASSWORD")); card.add(password);
        card.add(Box.createVerticalStrut(22)); card.add(signIn);
        card.add(Box.createVerticalStrut(12)); card.add(register);
        page.add(card);
        return page;
    }

    private JPanel buildRegister() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BG);
        JPanel card = formCard();
        JLabel brand = heading("EXAMFLOW", 25, NAVY);
        JLabel title = heading("Create your account", 23, TEXT);
        JTextField name = inputField();
        JTextField username = inputField();
        JPasswordField password = passwordField();
        JButton create = primaryButton("Create Account");
        JButton back = linkButton("Back to sign in");
        create.addActionListener(e -> {
            boolean ok = service.registerStudent(name.getText().trim(), username.getText().trim(),
                    new String(password.getPassword()));
            if (!ok) {
                showError("Registration failed. Enter all details, use a password of at least 4 characters, and choose a unique username.");
                return;
            }
            JOptionPane.showMessageDialog(frame, "Account created successfully. You can now sign in.",
                    "Registration complete", JOptionPane.INFORMATION_MESSAGE);
            cards.show(root, "login");
        });
        back.addActionListener(e -> cards.show(root, "login"));
        card.add(brand); card.add(Box.createVerticalStrut(16)); card.add(title);
        card.add(Box.createVerticalStrut(18)); card.add(fieldLabel("FULL NAME")); card.add(name);
        card.add(Box.createVerticalStrut(10)); card.add(fieldLabel("USERNAME")); card.add(username);
        card.add(Box.createVerticalStrut(10)); card.add(fieldLabel("PASSWORD (MIN. 4 CHARACTERS)")); card.add(password);
        card.add(Box.createVerticalStrut(20)); card.add(create); card.add(Box.createVerticalStrut(10)); card.add(back);
        page.add(card);
        return page;
    }

    private void showDashboard() {
        if (currentUser.getRole() == User.Role.ADMIN) showAdminDashboard();
        else showStudentDashboard();
    }

    private JPanel dashboardShell(String pageTitle, String subtitle, JPanel content) {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(BG);
        JPanel sidebar = new JPanel();
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(28, 18, 24, 18));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        JLabel logo = heading("EXAMFLOW", 22, Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(8));
        JLabel portal = label("EXAMINATION PORTAL", 10, new Color(185, 198, 220));
        portal.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(portal);
        sidebar.add(Box.createVerticalStrut(38));
        JLabel user = label(currentUser.getName(), 14, Color.WHITE);
        user.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(user);
        JLabel role = label(currentUser.getRole().name(), 10, new Color(185, 198, 220));
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(role);
        sidebar.add(Box.createVerticalGlue());
        JButton logout = new JButton("  Log out");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setForeground(Color.WHITE);
        logout.setBackground(new Color(43, 62, 101));
        logout.setFocusPainted(false);
        logout.setBorder(new EmptyBorder(12, 14, 12, 14));
        logout.addActionListener(e -> {
            if (examTimer != null) examTimer.stop();
            currentUser = null;
            cards.show(root, "login");
        });
        sidebar.add(logout);

        JPanel main = new JPanel(new BorderLayout(0, 22));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(30, 34, 30, 34));
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(heading(pageTitle, 27, TEXT));
        header.add(Box.createVerticalStrut(5));
        header.add(label(subtitle, 13, MUTED));
        main.add(header, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        shell.add(sidebar, BorderLayout.WEST);
        shell.add(main, BorderLayout.CENTER);
        return shell;
    }

    private void showStudentDashboard() {
        JPanel content = new JPanel(new GridLayout(0, 2, 18, 18));
        content.setOpaque(false);
        int count = service.getQuestions().size();
        List<Attempt> myAttempts = service.getAttemptsFor(currentUser.getUsername());
        content.add(statCard("AVAILABLE QUESTIONS", String.valueOf(count), "Java multiple-choice exam"));
        content.add(statCard("MY ATTEMPTS", String.valueOf(myAttempts.size()), "Completed examinations"));
        content.add(actionCard("Take examination", "Answer MCQs and get your result instantly.",
                "Start Exam", e -> startExam()));
        content.add(actionCard("My results", "Review your previous scores and pass status.",
                "View Results", e -> showResults(false)));
        root.add(dashboardShell("Student dashboard", "Your learning and examination overview", content), "student");
        cards.show(root, "student");
    }

    private void showAdminDashboard() {
        JPanel content = new JPanel(new GridLayout(0, 2, 18, 18));
        content.setOpaque(false);
        content.add(statCard("QUESTION BANK", String.valueOf(service.getQuestions().size()), "MCQs available"));
        content.add(statCard("EXAM ATTEMPTS", String.valueOf(service.getAttempts().size()), "All student submissions"));
        content.add(actionCard("Manage questions", "Add questions to the examination question bank.",
                "Add Question", e -> addQuestionDialog()));
        content.add(actionCard("Student results", "View scores submitted by all students.",
                "View All Results", e -> showResults(true)));
        root.add(dashboardShell("Administrator dashboard", "Manage examinations and monitor performance", content), "admin");
        cards.show(root, "admin");
    }

    private JPanel statCard(String title, String value, String detail) {
        JPanel card = whiteCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(label(title, 11, MUTED));
        card.add(Box.createVerticalStrut(10));
        card.add(heading(value, 31, BLUE));
        card.add(Box.createVerticalStrut(4));
        card.add(label(detail, 12, MUTED));
        return card;
    }

    private JPanel actionCard(String title, String detail, String buttonText, java.awt.event.ActionListener action) {
        JPanel card = whiteCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(heading(title, 19, TEXT));
        card.add(Box.createVerticalStrut(10));
        card.add(label(detail, 12, MUTED));
        card.add(Box.createVerticalGlue());
        JButton button = primaryButton(buttonText);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(action);
        card.add(button);
        return card;
    }

    private void startExam() {
        examQuestions = new ArrayList<>(service.getQuestions());
        if (examQuestions.isEmpty()) {
            showError("There are no questions available right now.");
            return;
        }
        answers = new ArrayList<>();
        for (int i = 0; i < examQuestions.size(); i++) answers.add(-1);
        questionIndex = 0;
        secondsLeft = Math.max(300, examQuestions.size() * 60);
        root.add(buildExamScreen(), "exam");
        cards.show(root, "exam");
        renderQuestion();
        if (examTimer != null) examTimer.stop();
        examTimer = new Timer(1000, e -> {
            secondsLeft--;
            updateTimer();
            if (secondsLeft <= 0) {
                examTimer.stop();
                finishExam();
            }
        });
        examTimer.start();
    }

    private JPanel buildExamScreen() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(28, 42, 28, 42));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading("Java MCQ Examination", 25, TEXT), BorderLayout.WEST);
        timerLabel = label("", 16, BLUE);
        top.add(timerLabel, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        JPanel card = whiteCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        questionCounter = label("", 12, MUTED);
        questionText = heading("", 20, TEXT);
        questionText.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(questionCounter);
        card.add(Box.createVerticalStrut(18));
        card.add(questionText);
        card.add(Box.createVerticalStrut(24));
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Segoe UI", Font.PLAIN, 15));
            optionButtons[i].setForeground(TEXT);
            optionButtons[i].setBackground(Color.WHITE);
            optionButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            optionGroup.add(optionButtons[i]);
            final int selected = i;
            optionButtons[i].addActionListener(e -> answers.set(questionIndex, selected));
            card.add(optionButtons[i]);
            card.add(Box.createVerticalStrut(13));
        }
        page.add(card, BorderLayout.CENTER);

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        JButton exit = secondaryButton("Exit exam");
        exit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Exit this exam? Your current answers will not be submitted.",
                    "Exit examination", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (examTimer != null) examTimer.stop();
                showStudentDashboard();
            }
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JButton previous = secondaryButton("Previous");
        previous.addActionListener(e -> moveQuestion(-1));
        JButton next = primaryButton("Next");
        next.addActionListener(e -> moveQuestion(1));
        JButton submit = primaryButton("Submit exam");
        submit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Submit your examination now?",
                    "Confirm submission", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) finishExam();
        });
        right.add(previous); right.add(next); right.add(submit);
        controls.add(exit, BorderLayout.WEST);
        controls.add(right, BorderLayout.EAST);
        page.add(controls, BorderLayout.SOUTH);
        return page;
    }

    private void renderQuestion() {
        Question q = examQuestions.get(questionIndex);
        questionCounter.setText("QUESTION " + (questionIndex + 1) + " OF " + examQuestions.size());
        questionText.setText("<html><div style='width:650px'>" + escapeHtml(q.getText()) + "</div></html>");
        optionGroup.clearSelection();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText((char)('A' + i) + ".   " + q.getOptions().get(i));
            optionButtons[i].setSelected(answers.get(questionIndex) == i);
        }
        updateTimer();
    }

    private void moveQuestion(int delta) {
        int target = questionIndex + delta;
        if (target >= 0 && target < examQuestions.size()) {
            questionIndex = target;
            renderQuestion();
        }
    }

    private void updateTimer() {
        if (timerLabel == null) return;
        timerLabel.setText(String.format("TIME LEFT   %02d:%02d", secondsLeft / 60, secondsLeft % 60));
    }

    private void finishExam() {
        if (examTimer != null) examTimer.stop();
        Attempt result = service.submitExam(currentUser.getUsername(), answers);
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JPanel score = whiteCard();
        score.setLayout(new BoxLayout(score, BoxLayout.Y_AXIS));
        JLabel badge = heading(result.isPassed() ? "EXAM PASSED" : "KEEP PRACTISING", 24,
                result.isPassed() ? GREEN : new Color(205, 83, 83));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel marks = heading(result.getScore() + " / " + result.getTotal(), 42, NAVY);
        marks.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel percentage = label(String.format("%.1f%% score", result.getPercentage()), 15, MUTED);
        percentage.setAlignmentX(Component.CENTER_ALIGNMENT);
        score.add(badge); score.add(Box.createVerticalStrut(18)); score.add(marks);
        score.add(Box.createVerticalStrut(6)); score.add(percentage);
        content.add(score);
        content.add(Box.createVerticalStrut(18));
        JButton back = primaryButton("Back to dashboard");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> showStudentDashboard());
        content.add(back);
        root.add(dashboardShell("Examination result", "Your submission has been recorded", content), "result");
        cards.show(root, "result");
    }

    private void showResults(boolean allStudents) {
        List<Attempt> attempts = allStudents ? service.getAttempts()
                : service.getAttemptsFor(currentUser.getUsername());
        String[] columns = allStudents
                ? new String[]{"Student", "Score", "Percentage", "Status", "Submitted"}
                : new String[]{"Score", "Percentage", "Status", "Submitted"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Attempt a : attempts) {
            if (allStudents) model.addRow(new Object[]{a.getUsername(), a.getScore() + "/" + a.getTotal(),
                    String.format("%.2f%%", a.getPercentage()), a.isPassed() ? "PASS" : "FAIL", a.getSubmittedAt()});
            else model.addRow(new Object[]{a.getScore() + "/" + a.getTotal(),
                    String.format("%.2f%%", a.getPercentage()), a.isPassed() ? "PASS" : "FAIL", a.getSubmittedAt()});
        }
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(229, 233, 240)));
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(scroll, BorderLayout.CENTER);
        JButton back = primaryButton("Back to dashboard");
        back.addActionListener(e -> showDashboard());
        content.add(back, BorderLayout.SOUTH);
        root.add(dashboardShell(allStudents ? "All examination results" : "My results",
                attempts.size() + " examination attempt(s) found", content),
                allStudents ? "all-results" : "my-results");
        cards.show(root, allStudents ? "all-results" : "my-results");
    }

    private void addQuestionDialog() {
        JTextField question = new JTextField(28);
        JTextField[] options = {new JTextField(24), new JTextField(24), new JTextField(24), new JTextField(24)};
        JComboBox<String> correct = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Question")); form.add(question);
        for (int i = 0; i < 4; i++) {
            form.add(new JLabel("Option " + (char)('A' + i)));
            form.add(options[i]);
        }
        form.add(new JLabel("Correct answer")); form.add(correct);
        int choice = JOptionPane.showConfirmDialog(frame, form, "Add MCQ question",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        List<String> values = new ArrayList<>();
        for (JTextField option : options) values.add(option.getText().trim());
        try {
            service.addQuestion(new Question(question.getText().trim(), values, correct.getSelectedIndex()));
            JOptionPane.showMessageDialog(frame, "Question added successfully.",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            showAdminDashboard();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private JPanel formCard() {
        JPanel card = whiteCard();
        card.setPreferredSize(new Dimension(420, 470));
        card.setBorder(new EmptyBorder(32, 36, 32, 36));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        return card;
    }

    private JPanel whiteCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(231, 235, 242)),
                new EmptyBorder(24, 24, 24, 24)));
        return panel;
    }

    private JLabel heading(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = label(text, 10, MUTED);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        return label;
    }

    private JTextField inputField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(340, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235)),
                new EmptyBorder(8, 11, 8, 11)));
        return field;
    }

    private JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(340, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235)),
                new EmptyBorder(8, 11, 8, 11)));
        return field;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(BLUE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(TEXT);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 225, 235)),
                new EmptyBorder(11, 17, 11, 17)));
        return button;
    }

    private JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(BLUE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Please check", JOptionPane.WARNING_MESSAGE);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
