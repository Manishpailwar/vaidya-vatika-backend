-- ═══════════════════════════════════════════════════════
--  VAIDYA VATIKA DATABASE SETUP
--  Run this in MySQL Workbench or MySQL CLI before starting
--  the Spring Boot server
-- ═══════════════════════════════════════════════════════

-- Step 1: Create the database
CREATE DATABASE IF NOT EXISTS vaidya_vatika_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Step 2: Use the database
USE vaidya_vatika_db;

-- ─── NOTE ─────────────────────────────────────────────
-- Spring Boot with JPA (ddl-auto=update) will
-- AUTO-CREATE all tables when you first run the app.
-- You only need to run this file ONCE to create the DB.
-- ──────────────────────────────────────────────────────

SELECT 'Database vaidya_vatika_db created successfully! 🌿' AS message;
