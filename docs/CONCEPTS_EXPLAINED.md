# Java Syllabus Concepts Applied — Online Examination System

This document explains the Java concepts listed in the Group 2 project outline, how each concept fits an Online Examination System, and what to demonstrate during a project presentation.

> **Architecture note:** The current `main` branch is a Spring Boot web application (Java REST API + HTML/CSS/JavaScript), not a Java Swing desktop application. The concepts below explain the syllabus design in the supplied project outline. The implementation-status notes distinguish concepts that are present in the current repository from concepts that are only part of the proposed Swing/JDBC design. Do not claim Swing or JDBC is implemented in the current branch unless the code is changed to use them.

## Unit 1 — Classes, objects, arrays, Math class and String methods

### 1. Classes and objects
A **class** is a blueprint that defines data and behaviour. An **object** is an instance of a class.

In this system, likely domain classes are:
- `Question`: stores question text, four options and the correct option.
- `Exam`: represents an examination, such as Java Fundamentals, and can hold its questions and duration.
- `Result`: stores a student's score, total marks, percentage and submission time.

Example:
```java
class Question {
    private String text;
    private String[] options;
    private int correctOption;

    Question(String text, String[] options, int correctOption) {
        this.text = text;
        this.options = options;
        this.correctOption = correctOption;
    }
}
Question q1 = new Question(
    "Which method is the entry point of Java?",
    new String[]{"start()", "run()", "main()", "init()"},
    2
);
```
Here, `Question` is the class and `q1` is an object.

### 2. Arrays
An **array** stores a fixed-size sequence of values of the same type. An exam can use an array to store a student's selected option for each question.

```java
int[] answers = {-1, 2, 0, 3, -1};
// -1 means unanswered; 0..3 represent options A..D.
answers[0] = 1; // save the answer to question 1
```
Arrays are useful because question and answer positions correspond by index.

### 3. Math class
Java's built-in `Math` class provides numeric operations. The result percentage can be calculated as:

```java
double percentage = (score * 100.0) / totalQuestions;
long rounded = Math.round(percentage);
boolean passed = percentage >= 40.0;
```
The `100.0` ensures floating-point division rather than integer division. Check that the total is not zero before dividing.

### 4. String methods
Strings are used for names, usernames, question text and options. Useful methods include:
- `trim()`: removes leading/trailing whitespace from input.
- `isBlank()`: checks whether text is empty or whitespace only.
- `equalsIgnoreCase()`: compares usernames without case sensitivity.
- `length()`: checks password length.
- `toUpperCase()` / `toLowerCase()`: normalizes text for display or comparison.

```java
String username = enteredUsername.trim();
if (username.isBlank()) {
    System.out.println("Username is required");
}
boolean sameUser = username.equalsIgnoreCase(savedUsername);
```

## Unit 2 — Abstract class, inheritance, interface and packages

### 1. Abstract class `User`
An **abstract class** is a common base class that cannot be instantiated directly. It holds shared fields and methods for different user types.

```java
abstract class User {
    protected String name;
    protected String username;

    User(String name, String username) {
        this.name = name;
        this.username = username;
    }

    public abstract void openDashboard();
}
```

### 2. Inheritance: Admin and Student
**Inheritance** lets a child class reuse and specialize the fields and methods of a parent class.

```java
class Admin extends User {
    Admin(String name, String username) {
        super(name, username);
    }
    @Override
    public void openDashboard() {
        System.out.println("Admin dashboard: manage questions");
    }
}

class Student extends User {
    Student(String name, String username) {
        super(name, username);
    }
    @Override
    public void openDashboard() {
        System.out.println("Student dashboard: attempt exams");
    }
}
```
An Admin can manage the question bank; a Student can select and attempt examinations. Role checks must also be enforced in backend methods, not only by hiding GUI buttons.

### 3. Interface `Evaluable`
An **interface** defines a contract that implementing classes must fulfil. An exam/result can implement an evaluation operation.

```java
interface Evaluable {
    int evaluate(int[] answers);
}

class Exam implements Evaluable {
    private int[] correctAnswers;

    Exam(int[] correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    @Override
    public int evaluate(int[] answers) {
        int score = 0;
        for (int i = 0; i < correctAnswers.length && i < answers.length; i++) {
            if (answers[i] == correctAnswers[i]) score++;
        }
        return score;
    }
}
```

### 4. Package `exam.core`
A **package** groups related classes, avoids naming conflicts and makes a project easier to maintain. For example:
- `exam.core`: Question, Exam, Result and evaluation logic.
- `exam.users`: User, Admin and Student.
- `exam.ui`: login, admin and student screens.
- `exam.db`: database connection and repository/DAO classes.

The current repository uses the package root `com.group2.onlineexam`, with separate `model`, `service` and `web` packages.

## Unit 3 — Timer thread, custom exception and synchronized submission

### 1. Timer thread
A **thread** allows a task to run independently of the main UI flow. A countdown can update the remaining time and submit the exam when it reaches zero.

Example of the core countdown idea:
```java
for (int remaining = durationSeconds; remaining >= 0; remaining--) {
    System.out.println("Time left: " + remaining);
    Thread.sleep(1000);
}
```
In a Swing GUI, do not update Swing components directly from a background thread. Use `SwingUtilities.invokeLater(...)` or a Swing `Timer` to update the interface safely. In a web app, the browser can display the countdown, but the server must enforce the deadline; a client-side timer alone is not secure.

### 2. `InvalidLoginException`
A **custom exception** represents a specific application error. It can be thrown when credentials are invalid and handled by the login screen.

```java
class InvalidLoginException extends Exception {
    InvalidLoginException(String message) {
        super(message);
    }
}

void authenticate(String username, String password)
        throws InvalidLoginException {
    if (!credentialsAreValid(username, password)) {
        throw new InvalidLoginException("Invalid username or password");
    }
}
```
The UI should show a clear message and must not expose passwords or sensitive internal details.

### 3. `synchronized` submission
The `synchronized` keyword controls concurrent access to a critical section. It can prevent two threads from saving the same in-memory submission simultaneously.

```java
public synchronized Result submitExam(String username, int[] answers) {
    // Validate the attempt, evaluate answers and save one result.
    return createAndSaveResult(username, answers);
}
```
Synchronization protects shared in-memory state inside one application process. It does not replace database transactions, unique constraints or server-side duplicate-submission checks.

## Unit 4 — JDBC Statement and PreparedStatement

### 1. JDBC
**Java Database Connectivity (JDBC)** is Java's standard API for communicating with relational databases such as MySQL. Typical operations are:
1. Open a database connection.
2. Execute SQL.
3. Read returned rows.
4. Close resources.

### 2. Statement
A `Statement` executes a fixed SQL string. It is suitable for simple SQL that contains no user-provided values.

```java
String sql = "SELECT id, question_text FROM questions";
try (Statement statement = connection.createStatement();
     ResultSet rs = statement.executeQuery(sql)) {
    while (rs.next()) {
        System.out.println(rs.getString("question_text"));
    }
}
```

### 3. PreparedStatement
A `PreparedStatement` uses placeholders (`?`) for values. It is preferred for user input because values are bound separately from SQL, reducing SQL-injection risk.

```java
String sql = "SELECT id, name FROM users WHERE username = ?";
try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, username);
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            System.out.println(rs.getString("name"));
        }
    }
}
```
For saving a result:
```java
String sql = "INSERT INTO results(username, score, total) VALUES (?, ?, ?)";
try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, username);
    ps.setInt(2, score);
    ps.setInt(3, total);
    ps.executeUpdate();
}
```

### 4. Suggested database tables
- `users`: user ID, name, username, password hash and role.
- `questions`: question ID, question text, four options, correct option and category.
- `results`: result ID, user ID, score, total, percentage and submission timestamp.

Store password hashes, never plain-text passwords. Use foreign keys from results to users, and use transactions for operations that must succeed or fail together.

**Current repository status:** the present service stores data in a local Java-serialized file (`data/online-exam.dat`). It does not currently use JDBC, `Statement`, `PreparedStatement` or SQL tables. The SQL examples in this section explain the Unit 4 requirement; they are not claims that JDBC is already integrated.

## Unit 5 — Swing components, layouts and event handling

This unit describes a Java Swing desktop interface.

### 1. JRadioButton
A `JRadioButton` lets a student select one answer from several choices. Put the four buttons in a `ButtonGroup` so only one option can be selected for a question.

```java
JRadioButton a = new JRadioButton("Option A");
JRadioButton b = new JRadioButton("Option B");
JRadioButton c = new JRadioButton("Option C");
JRadioButton d = new JRadioButton("Option D");

ButtonGroup group = new ButtonGroup();
group.add(a);
group.add(b);
group.add(c);
group.add(d);
```

### 2. JButton and JLabel
- `JButton`: clickable controls such as Login, Start Exam, Next, Previous and Submit.
- `JLabel`: displays titles, question text, username, timer and score.

```java
JLabel questionLabel = new JLabel("Which method starts a Java application?");
JButton nextButton = new JButton("Next");
```

### 3. GridLayout and FlowLayout
- `GridLayout` arranges components in equal-sized rows and columns. It is useful for a question palette or a grid of answer controls.
- `FlowLayout` places components from left to right and wraps them when space is limited. It is useful for a row of buttons.

```java
JPanel questionPanel = new JPanel(new GridLayout(0, 1, 8, 8));
JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
```

### 4. ActionListener
An **ActionListener** responds to a button click or another action event.

```java
nextButton.addActionListener(event -> {
    // Save the selected answer and display the next question.
    showNextQuestion();
});
```
Typical events include login, registration, starting an exam, selecting an answer, moving between questions and submitting the test.

**Current repository status:** the current GitHub branch renders its interface with HTML, CSS and JavaScript in a browser. It does not contain a Swing desktop GUI or use `JRadioButton`, `JButton`, `JLabel`, `GridLayout`, `FlowLayout` or Swing `ActionListener`. The examples above are for the Swing version described in the supplied outline.

## How the modules work together

1. **Admin module:** an authenticated administrator creates and manages questions.
2. **Student login and test selection:** a student signs in and chooses an available exam.
3. **Timed test:** the application displays one question at a time and tracks the deadline.
4. **Automatic evaluation:** the submitted choices are compared with the answer key and the score/percentage are calculated.
5. **Result storage and display:** the result is saved against the student and shown in the result history.

## Presentation / viva quick revision

| Concept | One-line answer |
|---|---|
| Class | Blueprint for creating objects. |
| Object | An instance of a class. |
| Array | Fixed-size collection of same-type values. |
| Math | Built-in class for mathematical operations. |
| String methods | Methods for validating, comparing and formatting text. |
| Abstract class | Base class that can define shared state and abstract behaviour. |
| Inheritance | Reusing/extending a parent class in a child class. |
| Interface | Contract of methods that implementing classes provide. |
| Package | Namespace that organizes related Java classes. |
| Thread | Independent path of execution. |
| Custom exception | Application-specific error type. |
| synchronized | Restricts concurrent access to a critical section. |
| JDBC | Java API for database connectivity. |
| Statement | Executes a SQL statement. |
| PreparedStatement | Executes parameterized SQL using placeholders. |
| JRadioButton | Swing option control; use ButtonGroup for one selection. |
| JButton | Swing clickable button. |
| JLabel | Swing text/image display component. |
| GridLayout | Arranges components in equal cells. |
| FlowLayout | Arranges components left-to-right with wrapping. |
| ActionListener | Handles action events such as button clicks. |

## Before presenting

Be clear about the difference between the **project outline** and the **code currently in the GitHub main branch**. The current code is Spring Boot + browser UI and local serialized-file persistence. The outline describes Swing + JDBC. To claim all five units are implemented in the running application, the code must be migrated/extended to include the Swing UI and JDBC database layer, and those features must be tested.
