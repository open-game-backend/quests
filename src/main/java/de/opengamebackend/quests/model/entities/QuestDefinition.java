package de.opengamebackend.quests.model.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "quests_questdefinition")
public class QuestDefinition {
    @Id
    private String id;

    @ManyToOne(optional = false)
    private QuestCategory category;

    private int requiredProgress;
    private String rewardItemDefinitionId;
    private int rewardItemCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public QuestCategory getCategory() {
        return category;
    }

    public void setCategory(QuestCategory category) {
        this.category = category;
    }

    public int getRequiredProgress() {
        return requiredProgress;
    }

    public void setRequiredProgress(int requiredProgress) {
        this.requiredProgress = requiredProgress;
    }

    public String getRewardItemDefinitionId() {
        return rewardItemDefinitionId;
    }

    public void setRewardItemDefinitionId(String rewardItemDefinitionId) {
        this.rewardItemDefinitionId = rewardItemDefinitionId;
    }

    public int getRewardItemCount() {
        return rewardItemCount;
    }

    public void setRewardItemCount(int rewardItemCount) {
        this.rewardItemCount = rewardItemCount;
    }
}
