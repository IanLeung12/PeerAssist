# PeerAssist

A desktop platform for students to publish, peer-edit, and review each other's
assignments and work. Final project for ICS4UE.

Users sign up, upload PDF documents tagged by grade level and subject, and leave
marks and comments on other students' work. Documents can be searched, sorted,
and ranked by average review score.

## Tech stack

- **Java 8** + **Swing** (desktop GUI)
- **Supabase** — Auth (per-user accounts) + PostgREST data API, secured with
  Row Level Security
- **PDFBox** for rendering uploaded PDFs
- **Gson** for JSON
- **FlatLaf** for a modern look-and-feel
- **Maven** build (fat JAR via maven-shade-plugin)

## Running the app (users)

Download `PeerAssist.jar` from [releases](https://github.com/toxicface/PeerAssist/releases) and run it — no setup, no config:

```sh
java -jar PeerAssist.jar
```

Requires **Java 8 or newer** installed. Sign up with an email and password, then
log in.

## Building / hosting (developer)

The app talks to one shared Supabase project. To set up your own:

### 1. Supabase project

Create a free project at [supabase.com](https://supabase.com).

- **Auth → Sign In / Providers → Email**: turn **off** "Confirm email" (the
  desktop app has no email-link handling).
- **SQL Editor**: create the tables and Row Level Security policies:

```sql
create table profiles (
  id uuid primary key references auth.users on delete cascade,
  name text,
  grade integer,
  subjects text
);

create table documents (
  doc_id integer generated always as identity primary key,
  user_id uuid references profiles(id),
  name text,
  max_mark double precision,
  grade_level integer,
  subjects text,
  content text
);

create table reviews (
  review_id integer generated always as identity primary key,
  doc_id integer references documents(doc_id),
  reviewer uuid references profiles(id),
  mark double precision,
  comment text
);

alter table profiles  enable row level security;
alter table documents enable row level security;
alter table reviews   enable row level security;

create policy "read profiles"  on profiles  for select to authenticated using (true);
create policy "read documents" on documents for select to authenticated using (true);
create policy "read reviews"   on reviews   for select to authenticated using (true);

create policy "insert own profile"  on profiles  for insert to authenticated with check (auth.uid() = id);
create policy "insert own document" on documents for insert to authenticated with check (auth.uid() = user_id);
create policy "modify own document" on documents for update to authenticated using (auth.uid() = user_id);
create policy "delete own document" on documents for delete to authenticated using (auth.uid() = user_id);
create policy "insert own review"   on reviews   for insert to authenticated with check (auth.uid() = reviewer);
create policy "modify own review"   on reviews   for update to authenticated using (auth.uid() = reviewer);
create policy "delete own review"   on reviews   for delete to authenticated using (auth.uid() = reviewer);
```

- **Project Settings → API**: copy the **Project URL** and the **publishable**
  key (`sb_publishable_...`).

### 2. Config

Put the URL and publishable key in `src/main/resources/supabase.properties`:

```properties
supabase.url=https://YOUR-PROJECT-REF.supabase.co
supabase.publishableKey=YOUR-PUBLISHABLE-KEY
```

The publishable key is public by design — Row Level Security protects the data —
so this file is bundled inside the JAR and safe to commit. Never use the secret
key.

### 3. Build

```sh
mvn clean package
```

Produces a single runnable `target/PeerAssist3-1.0-SNAPSHOT.jar` (rename to
`PeerAssist.jar` for distribution).

## Screenshots

Home Page
![Home Page](https://github.com/user-attachments/assets/b76a0af9-4544-424a-8f1b-c8e10bd66437)

Document Viewer
![Document Viewer](https://github.com/user-attachments/assets/4f9ce503-0a89-4463-8561-938ef4825d4d)

## Video Demonstration

https://drive.google.com/file/d/145AGMovcY1yWezIWyJIA8QHlMM_jHFKF/view?usp=sharing
