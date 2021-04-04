package de.opengamebackend.quests.controller;

import de.opengamebackend.quests.model.entities.PlayerQuest;
import de.opengamebackend.quests.model.entities.QuestCategory;
import de.opengamebackend.quests.model.entities.QuestDefinition;
import de.opengamebackend.quests.model.requests.IncreaseQuestProgressRequest;
import de.opengamebackend.quests.model.requests.PutQuestCategoriesRequest;
import de.opengamebackend.quests.model.requests.PutQuestDefinitionsRequest;
import de.opengamebackend.quests.model.responses.*;
import de.opengamebackend.test.HttpRequestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
public class QuestControllerIntegrationTests {
    private MockMvc mvc;
    private TestEntityManager entityManager;
    private HttpRequestUtils httpRequestUtils;

    @MockBean
    private CollectionService collectionService;

    @Autowired
    public QuestControllerIntegrationTests(MockMvc mvc, TestEntityManager entityManager) {
        this.mvc = mvc;
        this.entityManager = entityManager;

        this.httpRequestUtils = new HttpRequestUtils();
    }

    @Test
    public void whenGetQuestCategories_thenOk() throws Exception {
        httpRequestUtils.assertGetOk(mvc, "/admin/questcategories", GetQuestCategoriesResponse.class);
    }

    @Test
    public void whenPutQuestCategories_thenOk() throws Exception {
        PutQuestCategoriesRequest request = new PutQuestCategoriesRequest();
        httpRequestUtils.assertPutOk(mvc, "/admin/questcategories", request);
    }

    @Test
    public void whenGetQuestDefinitions_thenOk() throws Exception {
        httpRequestUtils.assertGetOk(mvc, "/admin/questdefinitions", GetQuestDefinitionsResponse.class);
    }

    @Test
    public void whenPutQuestDefinitions_thenOk() throws Exception {
        PutQuestDefinitionsRequest request = new PutQuestDefinitionsRequest();
        httpRequestUtils.assertPutOk(mvc, "/admin/questdefinitions", request);
    }

    @Test
    public void whenCreateQuests_thenOk() throws Exception {
        httpRequestUtils.assertPostOk(mvc, "/client/createquests",null, CreateQuestsResponse.class,"testPlayer");
    }

    @Test
    public void whenGetPlayerQuests_thenOk() throws Exception {
        final String playerId = "testPlayer";
        httpRequestUtils.assertGetOk(mvc, "/admin/playerquests/" + playerId, GetPlayerQuestsResponse.class);
    }

    @Test
    public void givenQuestDefinition_whenIncreaseQuestProgress_thenOk() throws Exception {
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

        // WHEN & THEN
        IncreaseQuestProgressRequest request = new IncreaseQuestProgressRequest();
        httpRequestUtils.assertPostOk(mvc, "/server/increasequestprogress/testPlayer/" + questDefinition.getId(), request);
    }

    @Test
    public void whenFinishQuest_thenOk() throws Exception {
        // GIVEN
        final String playerId = "testPlayer";

        QuestCategory questCategory = new QuestCategory();
        questCategory.setId("testQuestCategory");
        entityManager.persist(questCategory);

        QuestDefinition questDefinition = new QuestDefinition();
        questDefinition.setId("testQuestDefinition");
        questDefinition.setCategory(questCategory);
        questDefinition.setRewardItemDefinitionId("testItemDefinition");
        entityManager.persist(questDefinition);

        PlayerQuest playerQuest = new PlayerQuest();
        playerQuest.setPlayerId(playerId);
        playerQuest.setDefinition(questDefinition);
        playerQuest.setGeneratedAt(OffsetDateTime.now());
        entityManager.persist(playerQuest);

        entityManager.flush();

        // WHEN & THEN
        httpRequestUtils.assertPostOk(mvc, "/client/finishquest/" + questDefinition.getId(),null,
                FinishQuestResponse.class, playerId);
    }
}
