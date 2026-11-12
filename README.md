# ExamFlow — Online Examination System

A responsive, browser-based Online Examination System built with **Java 17, Spring Boot, HTML, CSS and JavaScript**.

## Features

### Student
- Register and sign in.
- Responsive student dashboard with exam statistics.
- Attempt Java MCQs in a browser.
- Countdown timer, question progress, Previous/Next navigation.
- Instant score, percentage and pass/fail result.
- Personal examination history.

### Administrator
- Admin dashboard with question and submission counts.
- Add four-option MCQ questions and choose the correct answer.
- View all student examination results.

## Technology
- Java 17
- Spring Boot 3.4
- Spring Web REST API
- HTML5, CSS3 and JavaScript
- Maven
- Java object serialization for local persistence

## Requirements
- JDK 17 or later
- Maven 3.8+

## Run locally
From the project root (the folder containing `pom.xml`):

```bash
mvn clean package
java -jar target/online-examination-system-1.0.0.jar
```

Open this address in your browser:

```
http://localhost:8080
```

You can also run the main class from your IDE after Maven has downloaded dependencies.

## Default administrator account
- Username: `admin`
- Password: `admin123`

Students can create accounts from the registration screen.

## Data storage
The application creates `data/online-exam.dat` on first run. Accounts, questions and attempts are stored locally between runs. The data file is excluded from Git.

## Examination rules
- One mark per question.
- Four options per question, one correct answer.
- Pass mark: 40%.
- Timer: one minute per question, with a five-minute minimum.

## Important security note
This is an educational project/demo. It currently uses simple local authentication and Java object serialization. Do not expose it to the public internet or use real personal data without adding password hashing, authenticated sessions/authorization, input hardening and a production database.

## Project structure
```
src/main/java/com/group2/onlineexam/
  Main.java
  model/
  service/
  web/ExamController.java
src/main/resources/
  application.properties
  static/
    index.html
    styles.css
    app.js
```
