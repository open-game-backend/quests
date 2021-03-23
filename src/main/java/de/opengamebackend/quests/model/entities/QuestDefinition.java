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
}
