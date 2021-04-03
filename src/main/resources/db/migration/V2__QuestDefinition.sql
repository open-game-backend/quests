CREATE TABLE quests_questdefinition (
    id VARCHAR(100) NOT NULL,
    category_id VARCHAR(100) NOT NULL,
    required_progress INT(10) UNSIGNED NOT NULL,
    reward_item_definition_id VARCHAR(100) NOT NULL,
    reward_item_count INT(10) UNSIGNED NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES quests_questcategory(id)
);
