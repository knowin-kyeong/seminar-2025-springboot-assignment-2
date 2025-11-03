CREATE TABLE IF NOT EXISTS timetable_lecture(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timetable_id BIGINT NOT NULL,
    lecture_id BIGINT NOT NULL,

    CONSTRAINT timetable_lecture__fk__timetable_id FOREIGN KEY (timetable_id) REFERENCES timetables(id) ON DELETE CASCADE,
    CONSTRAINT timetable_lecture__fk__lecture_id FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE,

    UNIQUE KEY unique_timetable_lecture(timetable_id, lecture_id)
);