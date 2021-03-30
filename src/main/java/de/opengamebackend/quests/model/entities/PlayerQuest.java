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

    public long getId() {
        return id;
    }

    public QuestDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(QuestDefinition definition) {
        this.definition = definition;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
