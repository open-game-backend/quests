package de.opengamebackend.quests.model.repositories;

import de.opengamebackend.quests.model.entities.PlayerQuest;
import de.opengamebackend.quests.model.entities.QuestCategory;
import de.opengamebackend.quests.model.entities.QuestDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class PlayerQuestRepositoryTests {
    private TestEntityManager entityManager;
    private PlayerQuestRepository playerQuestRepository;

    @Autowired
    public PlayerQuestRepositoryTests(TestEntityManager entityManager, PlayerQuestRepository playerQuestRepository) {
        this.entityManager = entityManager;
        this.playerQuestRepository = playerQuestRepository;
    }

    @Test
    public void givenPlayerQuests_whenFindByPlayerId_thenReturnQuests() {
        // GIVEN
        QuestCategory questCategory = new QuestCategory();
        questCategory.setId("testQuestCategory");
        entityManager.persist(questCategory);

        QuestDefinition questDefinition = new QuestDefinition();
        questDefinition.setId("testQuestDefinition");
        questDefinition.setCategory(questCategory);
        questDefinition.setRewardItemDefinitionId("testItemDefinition");
        entityManager.persist(questDefinition);

        PlayerQuest playerQuest = new PlayerQuest();
        playerQuest.setPlayerId("testPlayer");
        playerQuest.setDefinition(questDefinition);
        playerQuest.setGeneratedAt(OffsetDateTime.now());
        entityManager.persist(playerQuest);

        entityManager.flush();

        // WHEN
        List<PlayerQuest> playerQuests = playerQuestRepository.findByPlayerId(playerQuest.getPlayerId());

        // THEN
        assertThat(playerQuests).isNotNull();
        assertThat(playerQuests).hasSize(1);
        assertThat(playerQuests.get(0)).isEqualTo(playerQuest);
    }

    @Test
    public void givenPlayerQuests_whenFindByPlayerIdAndDefinition_thenReturnQuests() {
        // GIVEN
        QuestCategory questCategory = new QuestCategory();
        questCategory.setId("testQuestCategory");
        entityManager.persist(questCategory);

        QuestDefinition questDefinition = new QuestDefinition();
        questDefinition.setId("testQuestDefinition");
        questDefinition.setCategory(questCategory);
        questDefinition.setRewardItemDefinitionId("testItemDefinition");
        entityManager.persist(questDefinition);

        PlayerQuest playerQuest = new PlayerQuest();
        playerQuest.setPlayerId("testPlayer");
        playerQuest.setDefinition(questDefinition);
        playerQuest.setGeneratedAt(OffsetDateTime.now());
        entityManager.persist(playerQuest);

        entityManager.flush();

        // WHEN
        List<PlayerQuest> playerQuests =
                playerQuestRepository.findByPlayerIdAndDefinition(playerQuest.getPlayerId(), questDefinition);

        // THEN
        assertThat(playerQuests).isNotNull();
        assertThat(playerQuests).hasSize(1);
        assertThat(playerQuests.get(0)).isEqualTo(playerQuest);
    }
}
