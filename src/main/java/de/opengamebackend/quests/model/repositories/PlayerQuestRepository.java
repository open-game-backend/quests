package de.opengamebackend.quests.model.repositories;

import de.opengamebackend.quests.model.entities.PlayerQuest;
import de.opengamebackend.quests.model.entities.QuestDefinition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerQuestRepository extends CrudRepository<PlayerQuest, Long> {
    List<PlayerQuest> findByPlayerId(String playerId);
    List<PlayerQuest> findByPlayerIdAndDefinition(String playerId, QuestDefinition questDefinition);
}
