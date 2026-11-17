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
4. Open http://localhost:8080

You can also build a runnable JAR with `mvn clean package` and run it using `java -jar target/examvault-1.0.0.jar`.

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
