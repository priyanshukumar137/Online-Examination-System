package com.group2.onlineexam.web;

import com.group2.onlineexam.model.Attempt;
import com.group2.onlineexam.model.Question;
import com.group2.onlineexam.model.User;
import com.group2.onlineexam.service.ExamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExamController {
    private final ExamService service = new ExamService();

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.username() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Enter username and password."));
        }
        return service.login(request.username(), request.password())
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(new AuthResponse(
                        user.getName(), user.getUsername(), user.getRole().name())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid username or password.")));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        boolean created = service.registerStudent(request.name(), request.username(), request.password());
        if (!created) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "Registration failed. Check your details, use a password of at least 4 characters, and choose a unique username."));
        }
        return ResponseEntity.ok(Map.of("message", "Account created. You can now sign in."));
    }

    @GetMapping("/questions")
    public List<QuestionView> questions() {
        return service.getQuestions().stream()
                .map(q -> new QuestionView(q.getText(), q.getOptions()))
                .toList();
    }

    @PostMapping("/exams/submit")
    public ResponseEntity<?> submit(@RequestBody SubmitRequest request) {
        if (request.username() == null || request.answers() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing exam submission details."));
        }
        Attempt result = service.submitExam(request.username(), request.answers());
        return ResponseEntity.ok(AttemptView.from(result));
    }

    @GetMapping("/results")
    public List<AttemptView> results(@RequestParam(required = false) String username) {
        List<Attempt> attempts = (username == null || username.isBlank())
                ? service.getAttempts() : service.getAttemptsFor(username);
        return attempts.stream().map(AttemptView::from).toList();
    }

    @PostMapping("/admin/questions")
    public ResponseEntity<?> addQuestion(@RequestBody AddQuestionRequest request) {
        try {
            service.addQuestion(new Question(request.text(), request.options(), request.correctOption()));
            return ResponseEntity.ok(Map.of("message", "Question added successfully."));
        } catch (IllegalArgumentException | NullPointerException ex) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    ex.getMessage() == null ? "Please complete all question fields." : ex.getMessage()));
        }
    }

    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String name, String username, String password) {}
    public record AuthResponse(String name, String username, String role) {}
    public record QuestionView(String text, List<String> options) {}
    public record SubmitRequest(String username, List<Integer> answers) {}
    public record AddQuestionRequest(String text, List<String> options, int correctOption) {}
    public record AttemptView(String username, int score, int total, double percentage,
                              boolean passed, String submittedAt) {
        static AttemptView from(Attempt a) {
            return new AttemptView(a.getUsername(), a.getScore(), a.getTotal(),
                    a.getPercentage(), a.isPassed(), a.getSubmittedAt());
        }
    }
}
