CREATE TABLE IF NOT EXISTS timetable_lecture(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timetable_id BIGINT NOT NULL,
    lecture_id BIGINT NOT NULL,

    fk_timetable FOREIGN KEY (timetable_id) REFERENCES timetables(id),
    fk_lecture FOREIGN KEY (lecture_id) REFERENCES lectures(id),

    UNIQUE KEY unique_timetable_lecture(timetable_id, lecture_id)
);