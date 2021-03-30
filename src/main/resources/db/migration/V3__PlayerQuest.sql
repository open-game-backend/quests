CREATE TABLE quests_playerquest (
    id INT NOT NULL AUTO_INCREMENT,
    quest_definition_id VARCHAR(100) NOT NULL,
    player_id VARCHAR(100) NOT NULL,
    current_progress INT(10) UNSIGNED NOT NULL,
    generated_at TIMESTAMP(3) NOT NULL,
    completed_at TIMESTAMP(3) NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (quest_definition_id) REFERENCES quests_questdefinition(id)
);

CREATE INDEX ix_quests_playerquest_player_id ON quests_playerquest (player_id);
