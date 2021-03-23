package de.opengamebackend.quests.model.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "quests_questcategory")
public class QuestCategory {
    @Id
    private String id;

    private Integer generationHourOfDay;
    private Integer generationDayOfWeek;
}
