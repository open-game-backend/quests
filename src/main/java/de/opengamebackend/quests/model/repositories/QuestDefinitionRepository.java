package de.opengamebackend.quests.model.repositories;

import de.opengamebackend.quests.model.entities.QuestCategory;
import de.opengamebackend.quests.model.entities.QuestDefinition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestDefinitionRepository extends CrudRepository<QuestDefinition, String> {
    List<QuestDefinition> findByCategory(QuestCategory category);
}
