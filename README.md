# ExamVault — Online Examination System

ExamVault is a Java Spring Boot online examination platform with a clean, responsive HTML/CSS/JavaScript interface.

## Features
- Student sign-up, sign-in and session-based authentication
- Role-based admin workspace for adding questions
- Ten practice tests across programming, computer science, web development and aptitude
- Per-test countdown timer with automatic submission at timeout
- Server-side answer evaluation and percentage calculation
- Attempt history stored in H2 locally or PostgreSQL in production
- Responsive dashboard with test library, stats and result history

## Live website

- Frontend: https://examvault4.netlify.app/
- The Netlify site proxies `/api/*` to the Spring Boot backend using `netlify.toml`.

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


## Production deployment

The repository includes a Render Blueprint (`render.yaml`) for the Spring Boot API and a Netlify API proxy (`netlify.toml`).

1. Create a PostgreSQL database with a provider such as Neon. Copy its connection details.
2. Use a JDBC URL in this format: `jdbc:postgresql://HOST/DBNAME?sslmode=require`. Set the database username and password separately.
3. In Render, choose **New → Blueprint**, connect this GitHub repository, and deploy the Blueprint. It creates the `examvault-backend-priyanshu` web service.
4. When prompted, set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` from the PostgreSQL database.
5. Confirm the backend health endpoint responds at `https://examvault-backend-priyanshu.onrender.com/api/health` with a JSON status of `ok`.
6. Redeploy the Netlify site from the latest GitHub `main` branch so the `netlify.toml` API proxy is applied.
7. Test student registration, sign-in, exam submission and results.

The Render Blueprint currently uses the free web-service plan, which may sleep when idle and take time to wake. PostgreSQL is external so student accounts and results are not stored on the service's temporary filesystem. Never commit database passwords to GitHub.

## Frontend fallback while the API is not deployed

If the Spring Boot API is unreachable, the browser UI uses a local demo mode so a student can create an account, sign in, open the dashboard, take sample tests, and view demo results. Demo accounts and results are stored only in that browser's local storage; they are not shared across devices and are not a production database. Deploy and connect the Spring Boot service plus PostgreSQL to enable real server-side accounts and persistent results.

Demo administrator (demo mode only): `admin@examvault.com` / `Admin@123`.
