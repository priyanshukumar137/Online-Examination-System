# Online Examination System

A Java 17 console-based Online Examination System built with Object-Oriented Programming and Maven.

## Features

### Student
- Register and log in.
- View the available Java MCQ examination.
- Attempt multiple-choice questions (A/B/C/D).
- Receive an instant score, percentage and pass/fail result.
- View personal examination history.

### Administrator
- Log in to the administrator dashboard.
- Add four-option MCQ questions.
- View the question bank.
- View results submitted by all students.

## Technology
- Java 17
- Apache Maven
- Object serialization for local persistence
- No external runtime dependencies

## Requirements
- JDK 17 or later
- Maven 3.8+

## Run
```bash
mvn clean package
java -jar target/online-examination-system-1.0.0.jar
```

Or compile with:
```bash
mvn clean compile
```

## Default administrator account
- Username: `admin`
- Password: `admin123`

Change this demo password before any real deployment. This educational console project stores passwords in its local serialized data file and is not intended for production use.

## Data storage
The application automatically creates `data/online-exam.dat` on first run. Student accounts, questions and attempts persist between runs. This data file is intentionally excluded from Git.

## Project structure
```
Online-Examination-System/
├── pom.xml
├── README.md
├── .gitignore
└── src/main/java/com/group2/onlineexam/
    ├── Main.java
    ├── model/
    │   ├── Attempt.java
    │   ├── Question.java
    │   └── User.java
    └── service/
        └── ExamService.java
```

## Examination rules
- Each question carries one mark.
- Each question has four options and one correct answer.
- A score of 40% or above is a pass.

## OOP concepts
- **Encapsulation:** private model fields with controlled access.
- **Abstraction:** examination operations are grouped in the service layer.
- **Composition:** the service manages users, questions and attempts.
- **Persistence:** serializable objects are stored locally.

## License
For educational use.
