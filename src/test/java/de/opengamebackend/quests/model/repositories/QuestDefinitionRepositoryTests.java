package de.opengamebackend.quests.model.repositories;

import de.opengamebackend.quests.model.entities.QuestCategory;
import de.opengamebackend.quests.model.entities.QuestDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class QuestDefinitionRepositoryTests {
    private TestEntityManager entityManager;
    private QuestDefinitionRepository questDefinitionRepository;

    @Autowired
    public QuestDefinitionRepositoryTests(TestEntityManager entityManager, QuestDefinitionRepository questDefinitionRepository) {
        this.entityManager = entityManager;
        this.questDefinitionRepository = questDefinitionRepository;
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

        entityManager.flush();

        // WHEN
        List<QuestDefinition> questDefinitions = questDefinitionRepository.findByCategory(questCategory);

        // THEN
        assertThat(questDefinitions).isNotNull();
        assertThat(questDefinitions).hasSize(1);
        assertThat(questDefinitions.get(0)).isEqualTo(questDefinition);
    }
}
