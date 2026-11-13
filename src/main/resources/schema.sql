-- =====================================================================
-- Student Attendance System - schema.sql (Group 5)
-- Single source of truth for the database structure.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS attendance_db;
USE attendance_db;

-- ---------- Accounts: one row per person who can log in ----------
CREATE TABLE IF NOT EXISTS users (
  id                   INT AUTO_INCREMENT PRIMARY KEY,
  username             VARCHAR(100) NOT NULL UNIQUE,      -- college email
  password_hash        VARCHAR(100) NOT NULL,             -- BCrypt hash, never plain text
  full_name            VARCHAR(100) NOT NULL,
  role                 ENUM('COORDINATOR','TEACHER','STUDENT') NOT NULL,
  must_change_password BOOLEAN NOT NULL DEFAULT TRUE,     -- force new password on first login
  failed_attempts      INT NOT NULL DEFAULT 0,            -- lock account after 5 wrong tries
  locked_until         DATETIME NULL,
  active               BOOLEAN NOT NULL DEFAULT TRUE      -- "delete" = deactivate, keeps history
);

-- ---------- Students (every student also has a row in users) ----------
CREATE TABLE IF NOT EXISTS student (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  user_id       INT NOT NULL UNIQUE,
  student_code  VARCHAR(20) NOT NULL UNIQUE,              -- e.g. MCA26-A-011
  roll_no       VARCHAR(10) NOT NULL,
  class_name    VARCHAR(30) NOT NULL,                     -- e.g. FYMCA-A
  UNIQUE (class_name, roll_no),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ---------- Subjects, and which teacher teaches which ----------
CREATE TABLE IF NOT EXISTS subject (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  subject_code  VARCHAR(20) NOT NULL UNIQUE,
  subject_name  VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS teacher_subject (
  teacher_id  INT NOT NULL,                               -- users.id of a TEACHER
  subject_id  INT NOT NULL,
  PRIMARY KEY (teacher_id, subject_id),
  FOREIGN KEY (teacher_id) REFERENCES users(id),
  FOREIGN KEY (subject_id) REFERENCES subject(id)
);

-- ---------- One row per lecture. The live QR belongs to a session. ----------
CREATE TABLE IF NOT EXISTS attendance_session (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  subject_id    INT NOT NULL,
  teacher_id    INT NOT NULL,
  class_name    VARCHAR(30) NOT NULL,
  session_date  DATE NOT NULL,
  start_time    DATETIME NOT NULL,
  end_time      DATETIME NULL,
  status        ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',
  latitude      DECIMAL(9,6) NULL,                        -- teacher's location (geofence centre)
  longitude     DECIMAL(9,6) NULL,
  radius_m      INT NOT NULL DEFAULT 100,
  FOREIGN KEY (subject_id) REFERENCES subject(id),
  FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- ---------- Attendance: one row per student per lecture ----------
CREATE TABLE IF NOT EXISTS attendance (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  session_id   INT NOT NULL,
  student_id   INT NOT NULL,
  status       ENUM('PRESENT','ABSENT') NOT NULL,
  method       ENUM('QR','MANUAL','IMPORT') NOT NULL,
  marked_by    INT NOT NULL,                              -- users.id (student for QR, teacher for MANUAL)
  marked_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  device_id    VARCHAR(64) NULL,                          -- browser cookie, QR scans only
  latitude     DECIMAL(9,6) NULL,                         -- where the student scanned
  longitude    DECIMAL(9,6) NULL,
  accuracy_m   INT NULL,
  distance_m   INT NULL,                                  -- distance from teacher, for review
  UNIQUE (session_id, student_id),                        -- no double entry
  UNIQUE (session_id, device_id),                         -- one phone cannot mark two students
  FOREIGN KEY (session_id) REFERENCES attendance_session(id),
  FOREIGN KEY (student_id) REFERENCES student(id),
  FOREIGN KEY (marked_by)  REFERENCES users(id)
);

-- ---------- Student asks teacher to mark manually ----------
CREATE TABLE IF NOT EXISTS attendance_request (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  session_id    INT NOT NULL,
  student_id    INT NOT NULL,
  reason        VARCHAR(255) NOT NULL,
  status        ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  requested_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  decided_by    INT NULL,
  decided_at    DATETIME NULL,
  decision_note VARCHAR(255) NULL,
  UNIQUE (session_id, student_id),                        -- one request per lecture
  FOREIGN KEY (session_id) REFERENCES attendance_session(id),
  FOREIGN KEY (student_id) REFERENCES student(id),
  FOREIGN KEY (decided_by) REFERENCES users(id)
);

-- ---------- Who changed what (filled by the AOP audit advice) ----------
CREATE TABLE IF NOT EXISTS audit_log (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  actor_id    INT NOT NULL,
  action      VARCHAR(50) NOT NULL,                       -- e.g. MARK_MANUAL, EDIT_ATTENDANCE
  entity      VARCHAR(50) NOT NULL,
  entity_id   INT NULL,
  old_value   VARCHAR(255) NULL,
  new_value   VARCHAR(255) NULL,
  reason      VARCHAR(255) NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (actor_id) REFERENCES users(id)
);

-- ---------- Dev-only first account (password: admin123) ----------
INSERT IGNORE INTO users (username, password_hash, full_name, role, must_change_password)
VALUES ('admin', '$2a$10$PwyZI3us1EIHaNCRzj9V3.vVdvX474Y9c50nlwOI5CCff/hdX1q6m',
        'Coordinator', 'COORDINATOR', FALSE);