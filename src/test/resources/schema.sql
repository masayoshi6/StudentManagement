CREATE TABLE IF NOT EXISTS students
(
   id INT PRIMARY KEY AUTO_INCREMENT,
   name VARCHAR(50) NOT NULL,
   kana_name VARCHAR(50) NOT NULL,
   nickname VARCHAR(50),
   email VARCHAR(50) NOT NUll,
   area VARCHAR(50),
   age INT,
   sex VARCHAR(10),
   remark TEXT,
   is_deleted boolean
);

CREATE TABLE IF NOT EXISTS students_courses
(
   id INT PRIMARY KEY  AUTO_INCREMENT,
   student_id INT NOT NULL,
   course_name VARCHAR(50) NOT NULL,
   course_start_at TIMESTAMP,
   course_end_at TIMESTAMP
);
