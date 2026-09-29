# Job Application Tracker (mini ATS)

This is a full-stack job tracker I built to practice working across the whole
stack instead of just backend or just frontend — recruiters can post jobs and
move applicants through a hiring pipeline, and candidates can browse jobs,
apply, upload a resume, and see where they stand.

I started this after an Employee Management System (the classic fresher
project everyone builds), and honestly wanted something a bit less generic
that actually forces you to think about roles, permissions, and real state
transitions instead of just CRUD forms.

## Stack

- Java 17 + Spring Boot 3 (REST API, Spring Security with JWT, Spring Data JPA)
- PostgreSQL for the actual database
- React (Vite) + React Router + Axios on the frontend
- Recharts for the analytics charts on the recruiter side

I originally built this with H2 so it'd run with zero setup, then switched to
Postgres once I wanted it to feel like something you could actually deploy,
not just a local demo. There's a docker-compose file so spinning up Postgres
is one command instead of installing it manually.

## What it actually does

**If you're a recruiter:**
- Post jobs, close them once you're done hiring
- See everyone who applied to a specific job
- Move each applicant through stages: Applied → Shortlisted → Interview → Hired/Rejected
- Rate candidates 1-5 stars and leave notes (candidates can see the notes)
- Download whatever resume they uploaded
- Filter applicants by current stage
- A small analytics page — funnel chart, applicants per job, rough hire rate

**If you're a candidate:**
- Browse open jobs, search by title/skill, filter by location, paginated
- Apply with one click
- Upload an actual resume file (PDF/DOC/DOCX, capped at 5MB)
- Track your application status and read any notes the recruiter left

Auth is JWT-based — login gives you a token, the frontend stores it and
attaches it to every request. Roles are checked on the backend, not just
hidden in the UI, since that's the part that actually matters for security.

## Running this locally

You'll need Docker (for Postgres), Java 17+, Maven, and Node 18+.

**1. Start the database**
```
docker compose up -d
```
This spins up Postgres with the database and user the app expects already
configured. No Docker? Install Postgres yourself and create a database called
`atsdb` with user `atsuser` / password `atspassword`, or override those with
env vars (see application.properties).

**2. Start the backend**
```
cd backend
mvn spring-boot:run
```
Runs on port 8080. It'll create all the tables itself on first run — nothing
to run manually.

**3. Start the frontend**
```
cd frontend
npm install
npm run dev
```
Runs on port 5173. Open that in your browser and you're good to go.

## A few implementation notes

- Resumes are stored on local disk under `backend/uploads/`, not S3 or
  anything like that. That's the first thing I'd swap out if this ever needed
  to run on more than one server, since local disk doesn't survive a redeploy
  on most hosting platforms.
- `spring.jpa.hibernate.ddl-auto=update` is doing schema management right now,
  which is fine at this stage but not something I'd trust for a real
  production app long-term — Flyway or Liquibase would be the proper fix.
- The JWT secret has a default value in application.properties so the app
  runs out of the box, but obviously that shouldn't be the actual secret used
  anywhere real — it's meant to be overridden via env var in any real deploy.
- There aren't any tests yet. That's the most obvious gap and probably what
  I'd tackle next if I kept working on this.

## Project layout

```
backend/
  src/main/java/com/atstracker/
    model/        User, Job, JobApplication entities + enums
    repository/   Spring Data JPA interfaces
    service/      business logic (auth, jobs, applications, file storage, analytics)
    controller/   REST endpoints
    security/     JWT filter, JWT utility, UserDetailsService
    config/       Spring Security setup
    dto/          request/response objects, kept separate from the entities

frontend/
  src/
    pages/        Login, Register, JobList, PostJob, RecruiterDashboard, CandidateDashboard, Analytics
    components/   Navbar
    context/      AuthContext — who's logged in, login/logout
    api/          axios instance that auto-attaches the JWT
```

## Why I made some of the choices I did

- Kept the resume upload dead simple (just local file storage) rather than
  wiring up S3 — didn't want to burn time on infra for a project I might
  redeploy multiple times while learning.
- Went with role checks baked into the controllers rather than a fancier
  permission system, since with only two roles it didn't need more than that.
- The analytics page is intentionally basic — a few charts that actually mean
  something (funnel, per-job breakdown, conversion rate) rather than a bunch
  of vanity numbers.

If you're reading this because you're considering hiring me — thanks for
taking the time to actually look at the code. Happy to walk through any part
of it.
