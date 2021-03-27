package de.opengamebackend.quests.controller;

import de.opengamebackend.collection.model.requests.PutItemDefinitionsRequest;
import de.opengamebackend.quests.model.entities.QuestCategory;
import de.opengamebackend.quests.model.repositories.QuestCategoryRepository;
import de.opengamebackend.quests.model.requests.PutQuestCategoriesRequest;
import de.opengamebackend.quests.model.requests.PutQuestCategoriesRequestItem;
import de.opengamebackend.quests.model.responses.GetQuestCategoriesResponse;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class QuestServiceTests {
    private QuestCategoryRepository questCategoryRepository;

    private QuestService questService;

    @BeforeEach
    public void beforeEach() {
        questCategoryRepository = mock(QuestCategoryRepository.class);

        questService = new QuestService(questCategoryRepository);
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
        QuestCategory dailyQuestCategory = mock(QuestCategory.class);
        when(dailyQuestCategory.getId()).thenReturn("testDailyQuestCategory");

        QuestCategory weeklyQuestCategory = mock(QuestCategory.class);
        when(weeklyQuestCategory.getId()).thenReturn("testWeeklyQuestCategory");

        List<QuestCategory> existingCategories = Lists.list(dailyQuestCategory, weeklyQuestCategory);
        when(questCategoryRepository.findAll()).thenReturn(existingCategories);

        PutQuestCategoriesRequestItem requestedDailyQuestCategory = mock(PutQuestCategoriesRequestItem.class);
        when(requestedDailyQuestCategory.getId()).thenReturn("testDailyQuestCategory");

        PutQuestCategoriesRequestItem requestedWeeklyQuestCategory = mock(PutQuestCategoriesRequestItem.class);
        when(requestedWeeklyQuestCategory.getId()).thenReturn("testWeeklyQuestCategory");

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
}
