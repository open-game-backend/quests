package de.opengamebackend.quests.model.repositories;

import de.opengamebackend.quests.model.entities.QuestDefinition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestDefinitionRepository extends CrudRepository<QuestDefinition, String> {
}
