package de.opengamebackend.quests.controller;

import de.opengamebackend.net.ApiException;
import de.opengamebackend.quests.model.entities.QuestCategory;
import de.opengamebackend.quests.model.entities.QuestDefinition;
import de.opengamebackend.quests.model.repositories.QuestCategoryRepository;
import de.opengamebackend.quests.model.repositories.QuestDefinitionRepository;
import de.opengamebackend.quests.model.requests.PutQuestCategoriesRequest;
import de.opengamebackend.quests.model.requests.PutQuestCategoriesRequestItem;
import de.opengamebackend.quests.model.requests.PutQuestDefinitionsRequest;
import de.opengamebackend.quests.model.requests.PutQuestDefinitionsRequestItem;
import de.opengamebackend.quests.model.responses.GetQuestCategoriesResponse;
import de.opengamebackend.quests.model.responses.GetQuestDefinitionsResponse;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class QuestServiceTests {
    private QuestCategoryRepository questCategoryRepository;
    private QuestDefinitionRepository questDefinitionRepository;

    private QuestService questService;

    @BeforeEach
    public void beforeEach() {
        questCategoryRepository = mock(QuestCategoryRepository.class);
        questDefinitionRepository = mock(QuestDefinitionRepository.class);

        questService = new QuestService(questCategoryRepository, questDefinitionRepository);
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
}
