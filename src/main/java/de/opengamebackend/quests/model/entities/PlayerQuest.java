package de.opengamebackend.quests.model.entities;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "quests_playerquest")
public class PlayerQuest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    private QuestDefinition definition;

    private String playerId;
    private int currentProgress;
    private OffsetDateTime generatedAt;
    private OffsetDateTime completedAt;
}
