# PeerAssist

A desktop platform for students to publish, peer-edit, and review each other's
assignments and work. Final project for ICS4UE.

Users sign up, upload PDF documents tagged by grade level and subject, and leave
marks and comments on other students' work. Documents can be searched, sorted,
and ranked by average review score.

## Tech stack

- **Java 8** + **Swing** (desktop GUI)
- **PostgreSQL** (cloud, Supabase free tier) for persistence
- **PDFBox** for rendering uploaded PDFs
- **jBCrypt** for password hashing
- **FlatLaf** for a modern look-and-feel
- **Maven** build

## Setup

### 1. Database

Create a free PostgreSQL database (e.g. [Supabase](https://supabase.com) or
[Neon](https://neon.tech)). The app creates its tables (`users`, `documents`,
`reviews`) automatically on first run.

### 2. Connection config

Copy `db.properties.example` to `db.properties` in the project root, then fill in
your connection URL:

```properties
db.url=jdbc:postgresql://HOST:5432/postgres?user=USER&password=PASSWORD&sslmode=require
```

For Supabase: **Connect → JDBC tab → Session pooler** (port 5432).

`db.properties` is gitignored — it holds your database password, never commit it.
Alternatively, set the `PEERASSIST_DB_URL` environment variable instead of using
the file.

### 3. Run

Build and run with Maven, or run `org.example.PeerAssist` from your IDE:

```sh
mvn clean compile
```

## Screenshots

Home Page
![Home Page](https://github.com/user-attachments/assets/b76a0af9-4544-424a-8f1b-c8e10bd66437)

Document Viewer
![Document Viewer](https://github.com/user-attachments/assets/4f9ce503-0a89-4463-8561-938ef4825d4d)

## Video Demonstration

https://drive.google.com/file/d/145AGMovcY1yWezIWyJIA8QHlMM_jHFKF/view?usp=sharing
