CREATE TABLE IF NOT EXISTS time_space(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lecture_id BIGINT NOT NULL,
    day_of_week INT NOT NULL,
    start_time INT NOT NULL,
    end_time INT NOT NULL,
    location VARCHAR(512) NULL,

    fk_lecture FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE
);