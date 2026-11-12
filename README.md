# Online Examination System

A desktop-based Online Examination System built with **Java 17, Java Swing and Maven**.

## Interface

The application opens in a modern blue-and-white desktop interface with:
- Student sign-in and registration.
- Student dashboard with question and attempt summaries.
- MCQ examination screen with Previous/Next navigation and a countdown timer.
- Result screen showing marks, percentage and pass/fail status.
- Admin dashboard to add MCQs and view all student results.
- Results tables for students and administrators.

## Technology
- Java 17
- Java Swing (desktop GUI)
- Apache Maven
- Java object serialization for local persistence
- No external runtime dependencies

## Requirements
- JDK 17 or later
- Maven 3.8+

## Run from VS Code / terminal
Open the project folder, then run:

```bash
mvn clean package
java -jar target/online-examination-system-1.0.0.jar
```

Or run directly from Maven:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.group2.onlineexam.Main"
```

## Default administrator account
- Username: `admin`
- Password: `admin123`

Change this demo password before any real deployment. This educational project stores passwords in its local serialized data file and is not intended for production use.

## Data storage
The application creates `data/online-exam.dat` on first run. Student accounts, questions and attempts persist between runs. This data file is intentionally excluded from Git.

## Project structure
```
Online-Examination-System/
├── pom.xml
├── README.md
├── .gitignore
└── src/main/java/com/group2/onlineexam/
    ├── Main.java
    ├── ui/
    │   └── OnlineExamApp.java
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
- The exam timer is one minute per question, with a five-minute minimum.

## OOP concepts
- **Encapsulation:** private model fields with controlled access.
- **Abstraction:** examination operations are grouped in the service layer.
- **Composition:** the service manages users, questions and attempts.
- **Persistence:** serializable objects are stored locally.

## License
For educational use.
