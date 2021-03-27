package de.opengamebackend.quests.model.repositories;

import de.opengamebackend.quests.model.entities.QuestCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestCategoryRepository extends CrudRepository<QuestCategory, String> {
}
