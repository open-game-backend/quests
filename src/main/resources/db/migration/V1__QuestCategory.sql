CREATE TABLE quests_questcategory (
    id VARCHAR(100) NOT NULL,
    generation_hour_of_day INT(2) UNSIGNED NULL,
    generation_day_of_week INT(2) UNSIGNED NULL,

    PRIMARY KEY (id)
);
