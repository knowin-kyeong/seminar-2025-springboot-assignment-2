CREATE TABLE IF NOT EXISTS lectures(

    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year INT NOT NULL,
    semester INT NOT NULL,
    lecture_number VARCHAR(32) NOT NULL,
    class_number VARCHAR(32) NOT NULL,
    title VARCHAR(256) NOT NULL,
    subtitle VARCHAR(256) NULL,
    credit INT NOT NULL,
    classification VARCHAR(255) NOT NULL,
    college VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    academic_course VARCHAR(255) NOT NULL,
    academic_year VARCHAR(255) NOT NULL,
	instructor VARCHAR(255) NOT NULL,

    UNIQUE KEY unique_lecture(year, semester, lecture_number, class_number)
);