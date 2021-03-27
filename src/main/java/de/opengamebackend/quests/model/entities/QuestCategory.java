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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getGenerationHourOfDay() {
        return generationHourOfDay;
    }

    public void setGenerationHourOfDay(Integer generationHourOfDay) {
        this.generationHourOfDay = generationHourOfDay;
    }

    public Integer getGenerationDayOfWeek() {
        return generationDayOfWeek;
    }

    public void setGenerationDayOfWeek(Integer generationDayOfWeek) {
        this.generationDayOfWeek = generationDayOfWeek;
    }
}
