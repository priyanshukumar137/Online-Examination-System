# ExamVault — Online Examination System

ExamVault is a Java Spring Boot online examination platform with a clean, responsive HTML/CSS/JavaScript interface.

## Features
- Student sign-up, sign-in and session-based authentication
- Role-based admin workspace for adding questions
- Five ready-to-use tests: Java, DSA, DBMS, Web Development and Computer Networks
- Per-test countdown timer with automatic submission at timeout
- Server-side answer evaluation and percentage calculation
- Persistent attempt history using file-backed H2 database
- Responsive dashboard with test library, stats and result history

## Live website

- Frontend: https://examvault4.netlify.app
- Note: This Netlify deployment serves the frontend. Backend-dependent features such as authentication, exam submissions, and result storage require the Spring Boot backend to be deployed and connected separately.

## Tech stack
- Java 17, Spring Boot 3.4.5
- Spring Web, Spring JDBC, Spring Security Crypto (BCrypt)
- H2 database
- HTML5, CSS3 and vanilla JavaScript

## Run locally
1. Install JDK 17+ and Maven.
2. Clone this repository.
3. From the project root, run:
   ```bash
   mvn spring-boot:run
   ```
4. Open http://localhost:8081

You can also build a runnable JAR with `mvn clean package` and run it using `java -jar target/examvault-1.0.0.jar --server.port=8081`.

## Demo administrator
- Email: `admin@examvault.com`
- Password: `Admin@123`

Change the demo password and use HTTPS before deploying publicly. Student accounts can be created from the sign-up screen.

## Database
H2 stores data in `./data/examvault`. The database directory is intentionally ignored by Git. Five sample exams and 25 starter questions are seeded automatically on first launch.

## API overview
- `POST /api/auth/signup`, `POST /api/auth/login`, `POST /api/auth/logout`
- `GET /api/auth/me`
- `GET /api/exams`, `GET /api/exams/{id}/questions`
- `POST /api/attempts`, `GET /api/results`
- `POST /api/admin/questions` (admin only)


## ExamVault features

- Student registration with name, email, phone number, age, and password.
- Separate student and administrator sign-in portals with role checks.
- Ten practice tests with ten MCQs per test.
- Student dashboard, timed attempts, and result history.
- Administrator question bank module for adding MCQs, answer choices, correct answers, and explanations.

### Default administrator account

- Email: `admin@examvault.com`
- Password: `Admin@123`

Change the default administrator password before deploying this project publicly.
