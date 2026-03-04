# Universal DB Manager

A lightweight, cross-platform desktop database management tool built with Java.
Supports **PostgreSQL (Neon)**, **MySQL**, and **SQLite** through a modern Swing UI or a full-featured CLI — all from a single self-contained JAR.

---

## Features

### Three Databases, One Tool

| Database | Driver | Cloud / Local |
|---|---|---|
| PostgreSQL (Neon) | `org.postgresql` 42.7.10 | Cloud (Neon serverless) or self-hosted |
| MySQL | `com.mysql` 9.0.0 | Local or remote |
| SQLite | `org.xerial` 3.45.1.0 | Local file |

### UI Mode — Modern Dashboard

- Database picker popup (PostgreSQL / MySQL / SQLite)
- Login dialog with JDBC URL, username, password
- Three-column dashboard: **DDL**, **DML**, **DQL** operations
- Live result log with clear button
- Logout / Switch DB without restarting
- All operations run off the UI thread — never freezes on slow networks
- Busy guard — prevents duplicate concurrent operations
- Live status indicator in header (`⏳ Running…` / `Ready`)

### CLI Mode — Full Terminal Interface

- 21-operation menu identical in functionality to the UI
- Works headlessly — usable over SSH, scripts, or any terminal

### DDL Operations

- Create / Drop / Rename / Truncate Table
- List All Tables
- Describe Table (columns + types)
- Add / Drop / Rename Column

### DML Operations

- Insert Record (dynamic — prompts for every column automatically)
- Update by ID / Update by Exact Match
- Delete by ID / Delete by Exact Match / Delete All Records

### DQL Operations

- Fetch All Records
- Count Records
- Find by ID / Exact Match / Pattern (LIKE) / Greater Than (numeric)

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 22 or later |
| Maven | 3.8+ (build only) |

Download Java 22: <https://adoptium.net>

---

## Build

```bash
git clone https://github.com/your-username/universal-db-manager.git
cd universal-db-manager/database_manager
mvn clean package
```

Output: `target/database_manager-1.0-SNAPSHOT.jar` (~19 MB, all dependencies bundled — no separate installs needed)

---

## Run

### UI Mode (default)

```bash
java -jar target/database_manager-1.0-SNAPSHOT.jar
```

### CLI Mode

```bash
java -jar target/database_manager-1.0-SNAPSHOT.jar --cli
```

### Windows — double-click launcher

Create `run.bat` next to the JAR:

```bat
@echo off
java -jar database_manager-1.0-SNAPSHOT.jar
pause
```

---

## Connection Setup

### PostgreSQL (Neon)

| Field | Value |
|---|---|
| JDBC URL | `jdbc:postgresql://ep-xxx.neon.tech/neondb?sslmode=require&prepareThreshold=0` |
| Username | `neondb_owner` |
| Password | your Neon password |

### MySQL

| Field | Value |
|---|---|
| JDBC URL | `jdbc:mysql://localhost:3306/mydb` |
| Username | `root` |
| Password | your MySQL password |

### SQLite

| Field | Value |
|---|---|
| JDBC URL | `jdbc:sqlite:mydb.db` or `jdbc:sqlite:/absolute/path/mydb.db` |
| Username | *(leave blank)* |
| Password | *(leave blank)* |

---

## Project Structure

```
database_manager/
├── src/main/java/com/
│   ├── Main.java                         # Entry point — routes to UI or CLI
│   ├── service/
│   │   ├── DatabaseService.java          # Common interface for all databases
│   │   ├── postgress/
│   │   │   ├── PostgresService.java      # Public service wrapper
│   │   │   ├── DataManager.java          # DML / DQL operations
│   │   │   ├── TableManager.java         # DDL operations
│   │   │   ├── Validator.java            # Table existence checks
│   │   │   └── PostSignalActivator.java  # CLI menu for PostgreSQL
│   │   ├── sql/
│   │   │   ├── MySQLService.java
│   │   │   ├── DataManager.java
│   │   │   ├── TableManager.java
│   │   │   └── MySqlSignalActivator.java
│   │   └── sqllite/
│   │       ├── SQLiteService.java
│   │       ├── DataManager.java
│   │       ├── TableManager.java
│   │       └── LiteSignalActivator.java
│   └── ui/
│       └── DatabaseManagerUI.java        # Full Swing dashboard
└── pom.xml
```

---

## Architecture

```
Main
 ├── UI Mode  ──► DatabaseManagerUI
 │                └── DatabaseService (interface)
 │                     ├── PostgresService  ──► DataManager + TableManager
 │                     ├── MySQLService     ──► DataManager + TableManager
 │                     └── SQLiteService    ──► DataManager + TableManager
 │
 └── CLI Mode ──► PostSignalActivator
                  MySqlSignalActivator
                  LiteSignalActivator
                  └── DataManager + TableManager (per database)
```

---

## Robustness & Performance

| Feature | Detail |
|---|---|
| Off-EDT execution | All DB calls run on `SwingWorker` background threads — UI never freezes |
| Busy guard | `AtomicBoolean` prevents double-click race conditions |
| Query timeout | 30 s timeout on every `PreparedStatement` — no hung queries |
| Connection check | `isValid(2)` before every operation — surfaces "connection lost" clearly |
| Driver registration | `ServicesResourceTransformer` merges all 3 JDBC service entries — no "no suitable driver" error from fat JAR |
| Silent logging | `slf4j-nop` suppresses PostgreSQL driver SLF4J output |

---

## Converting to .exe (Windows)

On a Windows machine with JDK 22 installed:

```cmd
jpackage ^
  --input . ^
  --main-jar database_manager-1.0-SNAPSHOT.jar ^
  --name "DatabaseManager" ^
  --app-version "1.0" ^
  --win-shortcut ^
  --win-menu ^
  --win-dir-chooser ^
  --type exe
```

Produces a self-contained installer (~200 MB) — **no Java required on the target machine**.

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 22 |
| UI | Java Swing (Nimbus L&F + custom gradient components) |
| Build | Maven 3 + `maven-shade-plugin` 3.5.1 |
| PostgreSQL | `org.postgresql:postgresql:42.7.10` |
| MySQL | `com.mysql:mysql-connector-j:9.0.0` |
| SQLite | `org.xerial:sqlite-jdbc:3.45.1.0` |
| Logging | `org.slf4j:slf4j-nop:2.0.17` |

---

## License

MIT License — free to use, modify, and distribute.
