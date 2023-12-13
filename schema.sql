-- Students Table
CREATE TABLE Students (
                          student_id INT AUTO_INCREMENT PRIMARY KEY,
                          first_name VARCHAR(255) NOT NULL,
                          last_name VARCHAR(255) NOT NULL,
                          registration_number VARCHAR(10) UNIQUE NOT NULL
);

-- Courses Table
CREATE TABLE Courses (
                         course_id INT AUTO_INCREMENT PRIMARY KEY,
                         course_code VARCHAR(10) NOT NULL,
                         course_name VARCHAR(255) NOT NULL
);

-- Appointments Table
CREATE TABLE Appointments (
                              appointment_id INT AUTO_INCREMENT PRIMARY KEY,
                              student_id INT,
                              course_id INT,
                              appointment_time DATETIME NOT NULL,
                              duration_minutes INT NOT NULL,
                              FOREIGN KEY (student_id) REFERENCES Students(student_id),
                              FOREIGN KEY (course_id) REFERENCES Courses(course_id)
);
