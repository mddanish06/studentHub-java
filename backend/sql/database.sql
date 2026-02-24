-- create DB
CREATE DATABASE student_app;
USE student_app;

-- state table
CREATE TABLE state_name (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE
);

-- master student table
CREATE TABLE student_master (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  age INT NOT NULL,
  dob DATE,
  address TEXT,
  state_id INT,
  phone VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (state_id) REFERENCES state_name(id) ON DELETE SET NULL
);

-- student_detail table (subjects)
CREATE TABLE student_detail (
  id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  subject_name VARCHAR(200),
  FOREIGN KEY (student_id) REFERENCES student_master(id) ON DELETE CASCADE
);

-- student_photo (multiple photos per student) stored as BLOB
CREATE TABLE student_photo (
  id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  file_name VARCHAR(255),
  data LONGBLOB,
  content_type VARCHAR(50),
  FOREIGN KEY (student_id) REFERENCES student_master(id) ON DELETE CASCADE
);

INSERT INTO state_name (name) VALUES ('Maharashtra'), ('Karnataka'), ('Delhi'), ('Tamil Nadu');