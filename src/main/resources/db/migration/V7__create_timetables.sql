CREATE TABLE IF NOT EXISTS timetables(

    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    year INT NOT NULL,
    semester INT NOT NULL,

    UNIQUE KEY unique_timetable(user_id, year, semester, name)
);