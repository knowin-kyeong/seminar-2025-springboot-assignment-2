CREATE TABLE IF NOT EXISTS locationtimes(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lecture_id BIGINT NOT NULL,
    day_of_week INT NOT NULL,
    start_time INT NOT NULL,
    end_time INT NOT NULL,
    location VARCHAR(512) NULL,

    CONSTRAINT locationtimes__fk__lecture_id FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE
);