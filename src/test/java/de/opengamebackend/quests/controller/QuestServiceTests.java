package de.opengamebackend.quests.controller;

import de.opengamebackend.collection.model.requests.AddCollectionItemsRequest;
import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import de.opengamebackend.quests.model.entities.PlayerQuest;
import de.opengamebackend.quests.model.entities.QuestCategory;
import de.opengamebackend.quests.model.entities.QuestDefinition;
import de.opengamebackend.quests.model.repositories.PlayerQuestRepository;
import de.opengamebackend.quests.model.repositories.QuestCategoryRepository;
import de.opengamebackend.quests.model.repositories.QuestDefinitionRepository;
import de.opengamebackend.quests.model.requests.*;
import de.opengamebackend.quests.model.responses.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class QuestServiceTests {
    private QuestCategoryRepository questCategoryRepository;
    private QuestDefinitionRepository questDefinitionRepository;
    private PlayerQuestRepository playerQuestRepository;

    private CollectionService collectionService;

    private QuestService questService;

    @BeforeEach
    public void beforeEach() {
        questCategoryRepository = mock(QuestCategoryRepository.class);
        questDefinitionRepository = mock(QuestDefinitionRepository.class);
        playerQuestRepository = mock(PlayerQuestRepository.class);

        collectionService = mock(CollectionService.class);

        questService = new QuestService(questCategoryRepository, questDefinitionRepository, playerQuestRepository,
                collectionService);
    }

    @Test
    public void givenQuestCategories_whenGetQuestCategories_thenReturnCategories() {
        // GIVEN
        QuestCategory dailyQuestCategory = mock(QuestCategory.class);
        when(dailyQuestCategory.getId()).thenReturn("testDailyQuestCategory");
        when(dailyQuestCategory.getGenerationHourOfDay()).thenReturn(6);
        when(dailyQuestCategory.getGenerationDayOfWeek()).thenReturn(null);

        QuestCategory weeklyQuestCategory = mock(QuestCategory.class);
        when(weeklyQuestCategory.getId()).thenReturn("testWeeklyQuestCategory");
        when(weeklyQuestCategory.getGenerationHourOfDay()).thenReturn(6);
        when(weeklyQuestCategory.getGenerationDayOfWeek()).thenReturn(1);

        when(questCategoryRepository.findAll()).thenReturn(Lists.list(dailyQuestCategory, weeklyQuestCategory));

        // WHEN
        GetQuestCategoriesResponse response = questService.getQuestCategories();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getCategories()).isNotNull();
        assertThat(response.getCategories()).hasSize(2);
        assertThat(response.getCategories().get(0).getId()).isEqualTo(dailyQuestCategory.getId());
        assertThat(response.getCategories().get(0).getGenerationHourOfDay()).isEqualTo(dailyQuestCategory.getGenerationHourOfDay());
        assertThat(response.getCategories().get(0).getGenerationDayOfWeek()).isEqualTo(dailyQuestCategory.getGenerationDayOfWeek());
        assertThat(response.getCategories().get(1).getId()).isEqualTo(weeklyQuestCategory.getId());
        assertThat(response.getCategories().get(1).getGenerationHourOfDay()).isEqualTo(weeklyQuestCategory.getGenerationHourOfDay());
        assertThat(response.getCategories().get(1).getGenerationDayOfWeek()).isEqualTo(weeklyQuestCategory.getGenerationDayOfWeek());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenQuestCategories_whenPutQuestCategories_thenAddsNewCategories() {
        // GIVEN
        PutQuestCategoriesRequestItem dailyQuestCategory = mock(PutQuestCategoriesRequestItem.class);
        when(dailyQuestCategory.getId()).thenReturn("testDailyQuestCategory");
        when(dailyQuestCategory.getGenerationHourOfDay()).thenReturn(6);
        when(dailyQuestCategory.getGenerationDayOfWeek()).thenReturn(null);

        PutQuestCategoriesRequestItem weeklyQuestCategory = mock(PutQuestCategoriesRequestItem.class);
        when(weeklyQuestCategory.getId()).thenReturn("testWeeklyQuestCategory");
        when(weeklyQuestCategory.getGenerationHourOfDay()).thenReturn(6);
        when(weeklyQuestCategory.getGenerationDayOfWeek()).thenReturn(1);

        PutQuestCategoriesRequest request = mock(PutQuestCategoriesRequest.class);
        when(request.getCategories()).thenReturn(Lists.list(dailyQuestCategory, weeklyQuestCategory));

        // WHEN
        questService.putQuestCategories(request);

        // THEN
        ArgumentCaptor<List<QuestCategory>> argument = ArgumentCaptor.forClass(List.class);
        verify(questCategoryRepository).saveAll(argument.capture());

        List<QuestCategory> savedCategories = argument.getValue();

        assertThat(savedCategories).isNotNull();
        assertThat(savedCategories).hasSize(2);
        assertThat(savedCategories.get(0).getId()).isEqualTo(dailyQuestCategory.getId());
        assertThat(savedCategories.get(0).getGenerationHourOfDay()).isEqualTo(dailyQuestCategory.getGenerationHourOfDay());
        assertThat(savedCategories.get(0).getGenerationDayOfWeek()).isEqualTo(dailyQuestCategory.getGenerationDayOfWeek());
        assertThat(savedCategories.get(1).getId()).isEqualTo(weeklyQuestCategory.getId());
        assertThat(savedCategories.get(1).getGenerationHourOfDay()).isEqualTo(weeklyQuestCategory.getGenerationHourOfDay());
        assertThat(savedCategories.get(1).getGenerationDayOfWeek()).isEqualTo(weeklyQuestCategory.getGenerationDayOfWeek());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenQuestCategories_whenPutQuestCategories_thenRemovesObsoleteCategories() {
        // GIVEN
        QuestCategory dailyQuestCategory = mock(QuestCategory.class);
        when(dailyQuestCategory.getId()).thenReturn("testDailyQuestCategory");

        QuestCategory weeklyQuestCategory = mock(QuestCategory.class);
        when(weeklyQuestCategory.getId()).thenReturn("testWeeklyQuestCategory");

        List<QuestCategory> existingCategories = Lists.list(dailyQuestCategory, weeklyQuestCategory);
        when(questCategoryRepository.findAll()).thenReturn(existingCategories);

        PutQuestCategoriesRequest request = mock(PutQuestCategoriesRequest.class);

        // WHEN
        questService.putQuestCategories(request);

        // THEN
        ArgumentCaptor<List<QuestCategory>> argument = ArgumentCaptor.forClass(List.class);
        verify(questCategoryRepository).deleteAll(argument.capture());

        List<QuestCategory> deletedDefinitions = argument.getValue();

        assertThat(deletedDefinitions).isNotNull();
        assertThat(deletedDefinitions).hasSize(2);
        assertThat(deletedDefinitions.get(0)).isEqualTo(dailyQuestCategory);
        assertThat(deletedDefinitions.get(1)).isEqualTo(weeklyQuestCategory);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenQuestCategories_whenPutQuestCategories_thenRetainsExistingCategories() {
        // GIVEN
        final String dailyQuestCategoryId = "testDailyQuestCategory";
        final String weeklyQuestCategoryId = "testWeeklyQuestCategory";

        QuestCategory dailyQuestCategory = mock(QuestCategory.class);
        when(dailyQuestCategory.getId()).thenReturn(dailyQuestCategoryId);

        QuestCategory weeklyQuestCategory = mock(QuestCategory.class);
        when(weeklyQuestCategory.getId()).thenReturn(weeklyQuestCategoryId);

        List<QuestCategory> existingCategories = Lists.list(dailyQuestCategory, weeklyQuestCategory);
        when(questCategoryRepository.findAll()).thenReturn(existingCategories);

        PutQuestCategoriesRequestItem requestedDailyQuestCategory = mock(PutQuestCategoriesRequestItem.class);
        when(requestedDailyQuestCategory.getId()).thenReturn(dailyQuestCategoryId);

        PutQuestCategoriesRequestItem requestedWeeklyQuestCategory = mock(PutQuestCategoriesRequestItem.class);
        when(requestedWeeklyQuestCategory.getId()).thenReturn(weeklyQuestCategoryId);

        PutQuestCategoriesRequest request = mock(PutQuestCategoriesRequest.class);
        when(request.getCategories()).thenReturn(Lists.list(requestedDailyQuestCategory, requestedWeeklyQuestCategory));

        // WHEN
        questService.putQuestCategories(request);

        // THEN
        ArgumentCaptor<List<QuestCategory>> argument = ArgumentCaptor.forClass(List.class);
        verify(questCategoryRepository).deleteAll(argument.capture());

        List<QuestCategory> deletedDefinitions = argument.getValue();

        assertThat(deletedDefinitions).isNotNull();
        assertThat(deletedDefinitions).doesNotContain(dailyQuestCategory, weeklyQuestCategory);
    }

    @Test
    public void givenQuestDefinitions_whenGetQuestDefinitions_thenReturnDefinitions() {
        // GIVEN
        QuestCategory testQuestCategory = mock(QuestCategory.class);
        when(testQuestCategory.getId()).thenReturn("testQuestCategory");

        QuestDefinition questDefinition1 = mock(QuestDefinition.class);
        when(questDefinition1.getId()).thenReturn("testQuestDefinition1");
        when(questDefinition1.getCategory()).thenReturn(testQuestCategory);
        when(questDefinition1.getRequiredProgress()).thenReturn(2);
        when(questDefinition1.getRewardItemDefinitionId()).thenReturn("testRewardItemDefinition1");
        when(questDefinition1.getRewardItemCount()).thenReturn(3);

        QuestDefinition questDefinition2 = mock(QuestDefinition.class);
        when(questDefinition2.getId()).thenReturn("testQuestDefinition2");
        when(questDefinition2.getCategory()).thenReturn(testQuestCategory);
        when(questDefinition2.getRequiredProgress()).thenReturn(4);
        when(questDefinition2.getRewardItemDefinitionId()).thenReturn("testRewardItemDefinition2");
        when(questDefinition2.getRewardItemCount()).thenReturn(5);

        when(questDefinitionRepository.findAll()).thenReturn(Lists.list(questDefinition1, questDefinition2));

        // WHEN
        GetQuestDefinitionsResponse response = questService.getQuestDefinitions();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getQuestDefinitions()).isNotNull();
        assertThat(response.getQuestDefinitions()).hasSize(2);
        assertThat(response.getQuestDefinitions().get(0).getId()).isEqualTo(questDefinition1.getId());
        assertThat(response.getQuestDefinitions().get(0).getCategory()).isEqualTo(questDefinition1.getCategory().getId());
        assertThat(response.getQuestDefinitions().get(0).getRequiredProgress()).isEqualTo(questDefinition1.getRequiredProgress());
        assertThat(response.getQuestDefinitions().get(0).getRewardItemDefinitionId()).isEqualTo(questDefinition1.getRewardItemDefinitionId());
        assertThat(response.getQuestDefinitions().get(0).getRewardItemCount()).isEqualTo(questDefinition1.getRewardItemCount());
        assertThat(response.getQuestDefinitions().get(1).getId()).isEqualTo(questDefinition2.getId());
        assertThat(response.getQuestDefinitions().get(1).getCategory()).isEqualTo(questDefinition2.getCategory().getId());
        assertThat(response.getQuestDefinitions().get(1).getRequiredProgress()).isEqualTo(questDefinition2.getRequiredProgress());
        assertThat(response.getQuestDefinitions().get(1).getRewardItemDefinitionId()).isEqualTo(questDefinition2.getRewardItemDefinitionId());
        assertThat(response.getQuestDefinitions().get(1).getRewardItemCount()).isEqualTo(questDefinition2.getRewardItemCount());
    }

    @Test
    public void givenUnknownQuestCategory_whenPutQuestDefinitions_thenThrowException() {
        // GIVEN
        PutQuestDefinitionsRequestItem questDefinition = mock(PutQuestDefinitionsRequestItem.class);
        when(questDefinition.getId()).thenReturn("testQuestDefinition");

        PutQuestDefinitionsRequest request = mock(PutQuestDefinitionsRequest.class);
        when(request.getQuestDefinitions()).thenReturn(Lists.newArrayList(questDefinition));

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> questService.putQuestDefinitions(request));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenQuestDefinitions_whenPutQuestDefinitions_thenAddsNewDefinitions() throws ApiException {
        // GIVEN
        String questCategoryId = "testQuestCategory";

        QuestCategory questCategory = mock(QuestCategory.class);
        when(questCategory.getId()).thenReturn(questCategoryId);
        when(questCategoryRepository.findAll()).thenReturn(Lists.list(questCategory));

        PutQuestDefinitionsRequestItem questDefinition1 = mock(PutQuestDefinitionsRequestItem.class);
        when(questDefinition1.getId()).thenReturn("testQuestDefinition1");
        when(questDefinition1.getCategory()).thenReturn(questCategoryId);
        when(questDefinition1.getRequiredProgress()).thenReturn(2);
        when(questDefinition1.getRewardItemDefinitionId()).thenReturn("testItemDefinitionId1");
        when(questDefinition1.getRewardItemCount()).thenReturn(3);

        PutQuestDefinitionsRequestItem questDefinition2 = mock(PutQuestDefinitionsRequestItem.class);
        when(questDefinition2.getId()).thenReturn("testQuestDefinition2");
        when(questDefinition2.getCategory()).thenReturn(questCategoryId);
        when(questDefinition2.getRequiredProgress()).thenReturn(4);
        when(questDefinition2.getRewardItemDefinitionId()).thenReturn("testItemDefinitionId2");
        when(questDefinition2.getRewardItemCount()).thenReturn(5);

        PutQuestDefinitionsRequest request = mock(PutQuestDefinitionsRequest.class);
        when(request.getQuestDefinitions()).thenReturn(Lists.list(questDefinition1, questDefinition2));

        // WHEN
        questService.putQuestDefinitions(request);

        // THEN
        ArgumentCaptor<List<QuestDefinition>> argument = ArgumentCaptor.forClass(List.class);
        verify(questDefinitionRepository).saveAll(argument.capture());

        List<QuestDefinition> savedDefinitions = argument.getValue();

        assertThat(savedDefinitions).isNotNull();
        assertThat(savedDefinitions).hasSize(2);
        assertThat(savedDefinitions.get(0).getId()).isEqualTo(questDefinition1.getId());
        assertThat(savedDefinitions.get(0).getCategory()).isEqualTo(questCategory);
        assertThat(savedDefinitions.get(0).getRequiredProgress()).isEqualTo(questDefinition1.getRequiredProgress());
        assertThat(savedDefinitions.get(0).getRewardItemDefinitionId()).isEqualTo(questDefinition1.getRewardItemDefinitionId());
        assertThat(savedDefinitions.get(0).getRewardItemCount()).isEqualTo(questDefinition1.getRewardItemCount());
        assertThat(savedDefinitions.get(1).getId()).isEqualTo(questDefinition2.getId());
        assertThat(savedDefinitions.get(1).getCategory()).isEqualTo(questCategory);
        assertThat(savedDefinitions.get(1).getRequiredProgress()).isEqualTo(questDefinition2.getRequiredProgress());
        assertThat(savedDefinitions.get(1).getRewardItemDefinitionId()).isEqualTo(questDefinition2.getRewardItemDefinitionId());
        assertThat(savedDefinitions.get(1).getRewardItemCount()).isEqualTo(questDefinition2.getRewardItemCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenQuestDefinitions_whenPutQuestDefinitions_thenRemovesObsoleteDefinitions() throws ApiException {
        // GIVEN
        QuestDefinition questDefinition1 = mock(QuestDefinition.class);
        when(questDefinition1.getId()).thenReturn("testQuestDefinition1");

        QuestDefinition questDefinition2 = mock(QuestDefinition.class);
        when(questDefinition2.getId()).thenReturn("testQuestDefinition2");

        List<QuestDefinition> existingDefinitions = Lists.list(questDefinition1, questDefinition2);
        when(questDefinitionRepository.findAll()).thenReturn(existingDefinitions);

        PutQuestDefinitionsRequest request = mock(PutQuestDefinitionsRequest.class);

        // WHEN
        questService.putQuestDefinitions(request);

        // THEN
        ArgumentCaptor<List<QuestDefinition>> argument = ArgumentCaptor.forClass(List.class);
        verify(questDefinitionRepository).deleteAll(argument.capture());

        List<QuestDefinition> deletedDefinitions = argument.getValue();

        assertThat(deletedDefinitions).isNotNull();
        assertThat(deletedDefinitions).containsExactly(questDefinition1, questDefinition2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenQuestDefinitions_whenPutQuestDefinitions_thenRetainsExistingDefinitions() throws ApiException {
        // GIVEN
        final String questDefinitionId1 = "testQuestDefinition1";
        final String questDefinitionId2 = "testQuestDefinition2";

        QuestDefinition questDefinition1 = mock(QuestDefinition.class);
        when(questDefinition1.getId()).thenReturn(questDefinitionId1);

        QuestDefinition questDefinition2 = mock(QuestDefinition.class);
        when(questDefinition2.getId()).thenReturn(questDefinitionId2);

        List<QuestDefinition> existingDefinitions = Lists.list(questDefinition1, questDefinition2);
        when(questDefinitionRepository.findAll()).thenReturn(existingDefinitions);

        String questCategoryId = "testQuestCategory";

        QuestCategory questCategory = mock(QuestCategory.class);
        when(questCategory.getId()).thenReturn(questCategoryId);
        when(questCategoryRepository.findAll()).thenReturn(Lists.list(questCategory));

        PutQuestDefinitionsRequestItem requestedQuestDefinition1 = mock(PutQuestDefinitionsRequestItem.class);
        when(requestedQuestDefinition1.getId()).thenReturn(questDefinitionId1);
        when(requestedQuestDefinition1.getCategory()).thenReturn(questCategoryId);

        PutQuestDefinitionsRequestItem requestedQuestDefinition2 = mock(PutQuestDefinitionsRequestItem.class);
        when(requestedQuestDefinition2.getId()).thenReturn(questDefinitionId2);
        when(requestedQuestDefinition2.getCategory()).thenReturn(questCategoryId);
        
        PutQuestDefinitionsRequest request = mock(PutQuestDefinitionsRequest.class);
        when(request.getQuestDefinitions()).thenReturn(Lists.list(requestedQuestDefinition1, requestedQuestDefinition2));

        // WHEN
        questService.putQuestDefinitions(request);

        // THEN
        ArgumentCaptor<List<QuestDefinition>> argument = ArgumentCaptor.forClass(List.class);
        verify(questDefinitionRepository).deleteAll(argument.capture());

        List<QuestDefinition> deletedDefinitions = argument.getValue();

        assertThat(deletedDefinitions).isNotNull();
        assertThat(deletedDefinitions).doesNotContain(questDefinition1, questDefinition2);
    }

    @Test
    public void givenMissingPlayerId_whenCreateQuests_thenThrowException() {
        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> questService.createQuests(""))
                .withMessage(ApiErrors.MISSING_PLAYER_ID_MESSAGE);
    }

    @Test
    public void givenIncompleteQuests_whenCreateQuests_thenReturnsIncompleteQuests() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";

        QuestCategory questCategory = mock(QuestCategory.class);
        when(questCategory.getId()).thenReturn("testQuestCategory");

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getId()).thenReturn("testQuestDefinition");
        when(questDefinition.getCategory()).thenReturn(questCategory);
        when(questDefinition.getRequiredProgress()).thenReturn(2);
        when(questDefinition.getRewardItemDefinitionId()).thenReturn("testRewardItem");
        when(questDefinition.getRewardItemCount()).thenReturn(3);

        PlayerQuest incompleteQuest = mock(PlayerQuest.class);
        when(incompleteQuest.getId()).thenReturn(4L);
        when(incompleteQuest.getDefinition()).thenReturn(questDefinition);
        when(incompleteQuest.getCurrentProgress()).thenReturn(1);
        when(incompleteQuest.getGeneratedAt()).thenReturn(OffsetDateTime.now().minusDays(1));

        when(playerQuestRepository.findByPlayerId(playerId)).thenReturn(Lists.list(incompleteQuest));

        // WHEN
        CreateQuestsResponse response = questService.createQuests(playerId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getQuests()).isNotNull();
        assertThat(response.getQuests()).hasSize(1);
        assertThat(response.getQuests().get(0).getId()).isEqualTo(incompleteQuest.getId());
        assertThat(response.getQuests().get(0).getQuestDefinitionId()).isEqualTo(questDefinition.getId());
        assertThat(response.getQuests().get(0).getQuestCategoryId()).isEqualTo(questCategory.getId());
        assertThat(response.getQuests().get(0).getRequiredProgress()).isEqualTo(questDefinition.getRequiredProgress());
        assertThat(response.getQuests().get(0).getRewardItemDefinitionId()).isEqualTo(questDefinition.getRewardItemDefinitionId());
        assertThat(response.getQuests().get(0).getRewardItemCount()).isEqualTo(questDefinition.getRewardItemCount());
        assertThat(response.getQuests().get(0).getCurrentProgress()).isEqualTo(incompleteQuest.getCurrentProgress());
        assertThat(response.getQuests().get(0).getGeneratedAt()).isEqualTo(incompleteQuest.getGeneratedAt());
        assertThat(response.getQuests().get(0).isNewQuest()).isFalse();
    }

    @Test
    public void givenCompleteQuests_whenCreateQuests_thenDoesNotReturnCompleteQuests() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";

        PlayerQuest completeQuest = mock(PlayerQuest.class);
        when(completeQuest.getCompletedAt()).thenReturn(OffsetDateTime.now().minusDays(1));

        when(playerQuestRepository.findByPlayerId(playerId)).thenReturn(Lists.list(completeQuest));

        // WHEN
        CreateQuestsResponse response = questService.createQuests(playerId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getQuests()).isNotNull();
        assertThat(response.getQuests()).isEmpty();
    }

    @Test
    public void givenNoQuests_whenCreateQuests_thenCreatesQuest() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";

        QuestCategory questCategory = mock(QuestCategory.class);
        when(questCategoryRepository.findAll()).thenReturn(Lists.list(questCategory));

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getCategory()).thenReturn(questCategory);
        when(questDefinitionRepository.findByCategory(questCategory)).thenReturn(Lists.list(questDefinition));

        // WHEN
        CreateQuestsResponse response = questService.createQuests(playerId);

        // THEN
        ArgumentCaptor<PlayerQuest> argumentCaptor = ArgumentCaptor.forClass(PlayerQuest.class);
        verify(playerQuestRepository).save(argumentCaptor.capture());
        PlayerQuest newPlayerQuest = argumentCaptor.getValue();

        assertThat(newPlayerQuest).isNotNull();
        assertThat(newPlayerQuest.getPlayerId()).isEqualTo(playerId);
        assertThat(newPlayerQuest.getDefinition()).isEqualTo(questDefinition);
        assertThat(newPlayerQuest.getCurrentProgress()).isEqualTo(0);
        assertThat(newPlayerQuest.getGeneratedAt()).isNotNull();
        assertThat(newPlayerQuest.getCompletedAt()).isNull();

        assertThat(response).isNotNull();
        assertThat(response.getQuests()).isNotNull();
        assertThat(response.getQuests()).hasSize(1);
        assertThat(response.getQuests().get(0).getId()).isEqualTo(newPlayerQuest.getId());
    }

    @Test
    public void givenOldDailyQuest_whenCreateQuests_thenCreatesQuest() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";

        QuestCategory dailyQuestCategory = mock(QuestCategory.class);
        when(dailyQuestCategory.getId()).thenReturn("testQuestCategory");
        when(dailyQuestCategory.getGenerationDayOfWeek()).thenReturn(null);
        when(questCategoryRepository.findAll()).thenReturn(Lists.list(dailyQuestCategory));

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getCategory()).thenReturn(dailyQuestCategory);
        when(questDefinitionRepository.findByCategory(dailyQuestCategory)).thenReturn(Lists.list(questDefinition));

        PlayerQuest oldPlayerQuest = mock(PlayerQuest.class);
        when(oldPlayerQuest.getDefinition()).thenReturn(questDefinition);
        when(oldPlayerQuest.getGeneratedAt()).thenReturn(OffsetDateTime.now().minusDays(2));
        when(oldPlayerQuest.getCompletedAt()).thenReturn(OffsetDateTime.now().minusDays(1));
        when(playerQuestRepository.findByPlayerId(playerId)).thenReturn(Lists.list(oldPlayerQuest));

        // WHEN
        CreateQuestsResponse response = questService.createQuests(playerId);

        // THEN
        ArgumentCaptor<PlayerQuest> argumentCaptor = ArgumentCaptor.forClass(PlayerQuest.class);
        verify(playerQuestRepository).save(argumentCaptor.capture());
        PlayerQuest newPlayerQuest = argumentCaptor.getValue();

        assertThat(newPlayerQuest).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.getQuests()).isNotNull();
        assertThat(response.getQuests()).hasSize(1);
    }

    @Test
    public void givenCurrentDailyQuest_whenCreateQuests_thenDoesNotCreateQuest() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";

        QuestCategory dailyQuestCategory = mock(QuestCategory.class);
        when(dailyQuestCategory.getId()).thenReturn("testQuestCategory");
        when(dailyQuestCategory.getGenerationDayOfWeek()).thenReturn(null);
        when(questCategoryRepository.findAll()).thenReturn(Lists.list(dailyQuestCategory));

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getCategory()).thenReturn(dailyQuestCategory);
        when(questDefinitionRepository.findByCategory(dailyQuestCategory)).thenReturn(Lists.list(questDefinition));

        PlayerQuest currentPlayerQuest = mock(PlayerQuest.class);
        when(currentPlayerQuest.getDefinition()).thenReturn(questDefinition);
        when(currentPlayerQuest.getGeneratedAt()).thenReturn(OffsetDateTime.now());
        when(playerQuestRepository.findByPlayerId(playerId)).thenReturn(Lists.list(currentPlayerQuest));

        // WHEN
        questService.createQuests(playerId);

        // THEN
        verify(playerQuestRepository, never()).save(any());
    }

    @Test
    public void givenOldWeeklyQuest_whenCreateQuests_thenCreatesQuest() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";

        QuestCategory weeklyQuestCategory = mock(QuestCategory.class);
        when(weeklyQuestCategory.getId()).thenReturn("testQuestCategory");
        when(weeklyQuestCategory.getGenerationDayOfWeek()).thenReturn(1);
        when(questCategoryRepository.findAll()).thenReturn(Lists.list(weeklyQuestCategory));

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getCategory()).thenReturn(weeklyQuestCategory);
        when(questDefinitionRepository.findByCategory(weeklyQuestCategory)).thenReturn(Lists.list(questDefinition));

        PlayerQuest oldPlayerQuest = mock(PlayerQuest.class);
        when(oldPlayerQuest.getDefinition()).thenReturn(questDefinition);
        when(oldPlayerQuest.getGeneratedAt()).thenReturn(OffsetDateTime.now().minusWeeks(2));
        when(oldPlayerQuest.getCompletedAt()).thenReturn(OffsetDateTime.now().minusDays(1));
        when(playerQuestRepository.findByPlayerId(playerId)).thenReturn(Lists.list(oldPlayerQuest));

        // WHEN
        CreateQuestsResponse response = questService.createQuests(playerId);

        // THEN
        ArgumentCaptor<PlayerQuest> argumentCaptor = ArgumentCaptor.forClass(PlayerQuest.class);
        verify(playerQuestRepository).save(argumentCaptor.capture());
        PlayerQuest newPlayerQuest = argumentCaptor.getValue();

        assertThat(newPlayerQuest).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.getQuests()).isNotNull();
        assertThat(response.getQuests()).hasSize(1);
    }

    @Test
    public void givenCurrentWeeklyQuest_whenCreateQuests_thenDoesNotCreateQuest() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";

        QuestCategory weeklyQuestCategory = mock(QuestCategory.class);
        when(weeklyQuestCategory.getId()).thenReturn("testQuestCategory");
        when(weeklyQuestCategory.getGenerationDayOfWeek()).thenReturn(1);
        when(questCategoryRepository.findAll()).thenReturn(Lists.list(weeklyQuestCategory));

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getCategory()).thenReturn(weeklyQuestCategory);
        when(questDefinitionRepository.findByCategory(weeklyQuestCategory)).thenReturn(Lists.list(questDefinition));

        PlayerQuest currentPlayerQuest = mock(PlayerQuest.class);
        when(currentPlayerQuest.getDefinition()).thenReturn(questDefinition);
        when(currentPlayerQuest.getGeneratedAt()).thenReturn(OffsetDateTime.now());
        when(playerQuestRepository.findByPlayerId(playerId)).thenReturn(Lists.list(currentPlayerQuest));

        // WHEN
        questService.createQuests(playerId);

        // THEN
        verify(playerQuestRepository, never()).save(any());
    }

    @Test
    public void givenPlayerQuests_whenGetPlayerQuests_thenReturnQuests() {
        // GIVEN
        final String playerId = "testPlayer";

        QuestCategory testQuestCategory = mock(QuestCategory.class);
        when(testQuestCategory.getId()).thenReturn("testQuestCategory");

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getId()).thenReturn("testQuestDefinition");
        when(questDefinition.getCategory()).thenReturn(testQuestCategory);
        when(questDefinition.getRequiredProgress()).thenReturn(8);
        when(questDefinition.getRewardItemDefinitionId()).thenReturn("testRewardItemDefinition");
        when(questDefinition.getRewardItemCount()).thenReturn(3);

        PlayerQuest playerQuest1 = mock(PlayerQuest.class);
        when(playerQuest1.getId()).thenReturn(4L);
        when(playerQuest1.getDefinition()).thenReturn(questDefinition);
        when(playerQuest1.getCurrentProgress()).thenReturn(5);
        when(playerQuest1.getGeneratedAt()).thenReturn(OffsetDateTime.now().minusDays(2));
        when(playerQuest1.getCompletedAt()).thenReturn(OffsetDateTime.now().minusDays(1));

        PlayerQuest playerQuest2 = mock(PlayerQuest.class);
        when(playerQuest2.getId()).thenReturn(6L);
        when(playerQuest2.getDefinition()).thenReturn(questDefinition);
        when(playerQuest2.getCurrentProgress()).thenReturn(7);
        when(playerQuest2.getGeneratedAt()).thenReturn(OffsetDateTime.now().minusDays(4));
        when(playerQuest2.getCompletedAt()).thenReturn(OffsetDateTime.now().minusDays(3));

        when(playerQuestRepository.findByPlayerId(playerId)).thenReturn(Lists.list(playerQuest1, playerQuest2));

        // WHEN
        GetPlayerQuestsResponse response = questService.getPlayerQuests(playerId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getQuests()).isNotNull();
        assertThat(response.getQuests()).hasSize(2);
        assertThat(response.getQuests().get(0).getId()).isEqualTo(playerQuest1.getId());
        assertThat(response.getQuests().get(0).getQuestCategoryId()).isEqualTo(playerQuest1.getDefinition().getCategory().getId());
        assertThat(response.getQuests().get(0).getQuestDefinitionId()).isEqualTo(playerQuest1.getDefinition().getId());
        assertThat(response.getQuests().get(0).getRequiredProgress()).isEqualTo(playerQuest1.getDefinition().getRequiredProgress());
        assertThat(response.getQuests().get(0).getCurrentProgress()).isEqualTo(playerQuest1.getCurrentProgress());
        assertThat(response.getQuests().get(0).getRewardItemDefinitionId()).isEqualTo(playerQuest1.getDefinition().getRewardItemDefinitionId());
        assertThat(response.getQuests().get(0).getRewardItemCount()).isEqualTo(playerQuest1.getDefinition().getRewardItemCount());
        assertThat(response.getQuests().get(0).getGeneratedAt()).isEqualTo(playerQuest1.getGeneratedAt());
        assertThat(response.getQuests().get(0).getCompletedAt()).isEqualTo(playerQuest1.getCompletedAt());
        assertThat(response.getQuests().get(1).getId()).isEqualTo(playerQuest2.getId());
        assertThat(response.getQuests().get(1).getQuestCategoryId()).isEqualTo(playerQuest2.getDefinition().getCategory().getId());
        assertThat(response.getQuests().get(1).getQuestDefinitionId()).isEqualTo(playerQuest2.getDefinition().getId());
        assertThat(response.getQuests().get(1).getRequiredProgress()).isEqualTo(playerQuest2.getDefinition().getRequiredProgress());
        assertThat(response.getQuests().get(1).getCurrentProgress()).isEqualTo(playerQuest2.getCurrentProgress());
        assertThat(response.getQuests().get(1).getRewardItemDefinitionId()).isEqualTo(playerQuest2.getDefinition().getRewardItemDefinitionId());
        assertThat(response.getQuests().get(1).getRewardItemCount()).isEqualTo(playerQuest2.getDefinition().getRewardItemCount());
        assertThat(response.getQuests().get(1).getGeneratedAt()).isEqualTo(playerQuest2.getGeneratedAt());
        assertThat(response.getQuests().get(1).getCompletedAt()).isEqualTo(playerQuest2.getCompletedAt());
    }

    @Test
    public void givenMissingPlayerId_whenIncreaseQuestProgress_thenThrowException() {
        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> questService.increaseQuestProgress("", null, null))
                .withMessage(ApiErrors.MISSING_PLAYER_ID_MESSAGE);
    }

    @Test
    public void givenUnknownQuestDefinition_whenIncreaseQuestProgress_thenThrowException() {
        // GIVEN
        final String questDefinitionId = "testQuestDefinition";

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> questService.increaseQuestProgress("testPlayer", questDefinitionId, null))
                .withMessage(ApiErrors.UNKNOWN_QUEST_DEFINITION_MESSAGE + questDefinitionId);
    }

    @Test
    public void givenAbsentQuest_whenIncreaseQuestProgress_thenDoNothing() throws ApiException {
        // GIVEN
        final String questDefinitionId = "testQuestDefinition";

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinitionRepository.findById(questDefinitionId)).thenReturn(Optional.of(questDefinition));

        // WHEN
        questService.increaseQuestProgress("testPlayer", questDefinitionId, null);

        // THEN
        verify(playerQuestRepository, never()).save(any());
    }

    @Test
    public void givenActiveQuest_whenIncreaseQuestProgress_thenUpdateProgress() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";
        final String questDefinitionId = "testQuestDefinition";

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getRequiredProgress()).thenReturn(6);
        when(questDefinitionRepository.findById(questDefinitionId)).thenReturn(Optional.of(questDefinition));

        PlayerQuest playerQuest = mock(PlayerQuest.class);
        when(playerQuest.getCurrentProgress()).thenReturn(2);
        when(playerQuestRepository.findByPlayerIdAndQuestDefinition(playerId, questDefinition)).thenReturn(Lists.list(playerQuest));

        IncreaseQuestProgressRequest request = mock(IncreaseQuestProgressRequest.class);
        when(request.getProgressMade()).thenReturn(3);

        // WHEN
        questService.increaseQuestProgress(playerId, questDefinitionId, request);

        // THEN
        verify(playerQuest).setCurrentProgress(playerQuest.getCurrentProgress() + request.getProgressMade());
        verify(playerQuestRepository).save(playerQuest);
    }

    @Test
    public void givenMissingPlayerId_whenFinishQuest_thenThrowException() {
        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> questService.finishQuest("", null))
                .withMessage(ApiErrors.MISSING_PLAYER_ID_MESSAGE);
    }

    @Test
    public void givenUnknownQuestDefinition_whenFinishQuest_thenThrowException() {
        // GIVEN
        final String questDefinitionId = "testQuestDefinition";

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> questService.finishQuest("testPlayer", questDefinitionId))
                .withMessage(ApiErrors.UNKNOWN_QUEST_DEFINITION_MESSAGE + questDefinitionId);
    }

    @Test
    public void givenMissingIncompleteQuest_whenFinishQuest_thenThrowException() {
        // GIVEN
        final String questDefinitionId = "testQuestDefinition";

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinitionRepository.findById(questDefinitionId)).thenReturn(Optional.of(questDefinition));

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> questService.finishQuest("testPlayer", questDefinitionId))
                .withMessage(ApiErrors.QUEST_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenInsufficientQuestProgress_whenFinishQuest_thenThrowException() {
        // GIVEN
        final String playerId = "testPlayer";
        final String questDefinitionId = "testQuestDefinition";

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getRequiredProgress()).thenReturn(1);
        when(questDefinitionRepository.findById(questDefinitionId)).thenReturn(Optional.of(questDefinition));

        PlayerQuest playerQuest = mock(PlayerQuest.class);
        when(playerQuestRepository.findByPlayerIdAndQuestDefinition(playerId, questDefinition)).thenReturn(Lists.list(playerQuest));

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> questService.finishQuest(playerId, questDefinitionId))
                .withMessage(ApiErrors.INSUFFICIENT_QUEST_PROGRESS_MESSAGE);
    }

    @Test
    public void givenSufficientQuestProgress_whenFinishQuest_thenGrantRewards() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";
        final String questDefinitionId = "testQuestDefinition";
        final int requiredProgress = 1;

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getRequiredProgress()).thenReturn(requiredProgress);
        when(questDefinition.getRewardItemDefinitionId()).thenReturn("testReward");
        when(questDefinition.getRewardItemCount()).thenReturn(2);
        when(questDefinitionRepository.findById(questDefinitionId)).thenReturn(Optional.of(questDefinition));

        PlayerQuest playerQuest = mock(PlayerQuest.class);
        when(playerQuest.getCurrentProgress()).thenReturn(requiredProgress);
        when(playerQuestRepository.findByPlayerIdAndQuestDefinition(playerId, questDefinition)).thenReturn(Lists.list(playerQuest));

        // WHEN
        questService.finishQuest(playerId, questDefinitionId);

        // THEN
        ArgumentCaptor<AddCollectionItemsRequest> argumentCaptor = ArgumentCaptor.forClass(AddCollectionItemsRequest.class);
        verify(collectionService).addCollectionItems(eq(playerId), argumentCaptor.capture());
        AddCollectionItemsRequest request = argumentCaptor.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getItemDefinitionId()).isEqualTo(questDefinition.getRewardItemDefinitionId());
        assertThat(request.getItemCount()).isEqualTo(questDefinition.getRewardItemCount());
    }

    @Test
    public void givenSufficientQuestProgress_whenFinishQuest_thenMarkCompleted() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";
        final String questDefinitionId = "testQuestDefinition";
        final int requiredProgress = 1;

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getRequiredProgress()).thenReturn(requiredProgress);
        when(questDefinitionRepository.findById(questDefinitionId)).thenReturn(Optional.of(questDefinition));

        PlayerQuest playerQuest = mock(PlayerQuest.class);
        when(playerQuest.getCurrentProgress()).thenReturn(requiredProgress);
        when(playerQuestRepository.findByPlayerIdAndQuestDefinition(playerId, questDefinition)).thenReturn(Lists.list(playerQuest));

        // WHEN
        questService.finishQuest(playerId, questDefinitionId);

        // THEN
        verify(playerQuest).setCompletedAt(any());
        verify(playerQuestRepository).save(playerQuest);
    }

    @Test
    public void givenSufficientQuestProgress_whenFinishQuest_thenReturnGrantedRewards() throws ApiException {
        // GIVEN
        final String playerId = "testPlayer";
        final String questDefinitionId = "testQuestDefinition";
        final int requiredProgress = 1;

        QuestDefinition questDefinition = mock(QuestDefinition.class);
        when(questDefinition.getRequiredProgress()).thenReturn(requiredProgress);
        when(questDefinition.getRewardItemDefinitionId()).thenReturn("testReward");
        when(questDefinition.getRewardItemCount()).thenReturn(2);
        when(questDefinitionRepository.findById(questDefinitionId)).thenReturn(Optional.of(questDefinition));

        PlayerQuest playerQuest = mock(PlayerQuest.class);
        when(playerQuest.getCurrentProgress()).thenReturn(requiredProgress);
        when(playerQuestRepository.findByPlayerIdAndQuestDefinition(playerId, questDefinition)).thenReturn(Lists.list(playerQuest));

        // WHEN
        FinishQuestResponse response = questService.finishQuest(playerId, questDefinitionId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getRewardItemDefinitionId()).isEqualTo(questDefinition.getRewardItemDefinitionId());
        assertThat(response.getRewardItemCount()).isEqualTo(questDefinition.getRewardItemCount());
    }
}
