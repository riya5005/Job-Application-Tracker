# Job Application Tracker (mini ATS)

Full-stack app where recruiters post jobs and track applicants through a hiring
pipeline (Applied → Shortlisted → Interview → Hired/Rejected), and candidates
browse jobs, apply, and track their own application status.

Built with Spring Boot (Java) + React + SQL, with JWT-based auth and two
roles: RECRUITER and CANDIDATE.

## Stack

- **Backend:** Spring Boot 3, Spring Security + JWT, Spring Data JPA, PostgreSQL
- **Frontend:** React 18 + Vite, React Router, Axios, Recharts
- **DB:** PostgreSQL 16 (via docker-compose for local dev). Connection settings
  can be overridden with env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
  `JWT_SECRET`, `UPLOAD_DIR`) so nothing sensitive has to live in the repo.

## Features

- JWT auth with two roles (recruiter / candidate), role checks enforced server-side
- Candidates: browse jobs, search by title/skill/location (paginated), apply,
  upload a resume (PDF/DOC/DOCX, 5MB max), track application status, read recruiter feedback
- Recruiters: post/close jobs, review applicants per job, move them through the
  pipeline, rate candidates 1-5, leave notes, download resumes, filter by stage
- Analytics page: pipeline funnel, applicants per job, hiring conversion, average rating
- All users, jobs, applications (with candidate emails) and rating/notes are stored in Postgres

## Running it

### 1. Database (needs Docker)
```
docker compose up -d
```
This starts Postgres on port 5432 with the database/user the app expects.
No Docker? Install PostgreSQL locally and create a database `atsdb` with user
`atsuser` / password `atspassword` (or set the env vars above to match yours).

### 2. Backend
```
cd backend
mvn spring-boot:run
```
Runs on `http://localhost:8080`. Tables are created automatically on first run.
Uploaded resumes are saved in `backend/uploads/`.

### 3. Frontend
```
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173`.

## How the pieces fit together

- **Auth**: register/login returns a JWT, which the frontend stores in
  localStorage and attaches to every request via an axios interceptor
  (`frontend/src/api/axios.js`). `JwtFilter` on the backend reads it back out
  and sets up the Spring Security context per-request — no server sessions.
- **Roles**: a `User` is either `RECRUITER` or `CANDIDATE`. Controllers check
  `user.getRole()` before letting someone post a job or apply to one — this
  is enforced backend-side, not just hidden in the UI, since that's the part
  that actually matters.
- **Applications**: `JobApplication` is the join between `User` (candidate)
  and `Job`, holding the current pipeline `status`. Recruiters move it forward
  with a PATCH; candidates just read their own list.

## Things I'd add if this were a real product (i.e. good talking points for an interview)

- Move resume storage from local disk to S3 (local disk won't survive redeploys)
- Flyway migrations instead of `ddl-auto=update`
- Email notifications when status changes
- Pagination on the applicants list too (only the job listing is paginated right now)
- Refresh tokens instead of a single long-lived JWT
- Tests — there aren't any yet, which is the most obvious gap right now

## Project structure

```
backend/
  src/main/java/com/atstracker/
    model/        entities (User, Job, JobApplication + enums)
    repository/   Spring Data JPA interfaces
    service/      business logic
    controller/   REST endpoints
    security/     JWT filter, JWT util, UserDetailsService
    config/       Spring Security config
    dto/          request/response shapes, kept separate from entities

frontend/
  src/
    pages/        Login, Register, JobList, PostJob, RecruiterDashboard, CandidateDashboard
    components/    Navbar
    context/       AuthContext (who's logged in, login/logout)
    api/           axios instance with the JWT interceptor
```
