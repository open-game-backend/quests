package de.opengamebackend.quests.controller;

import de.opengamebackend.net.ApiErrors;
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
import de.opengamebackend.quests.model.responses.GetQuestCategoriesResponseItem;
import de.opengamebackend.quests.model.responses.GetQuestDefinitionsResponse;
import de.opengamebackend.quests.model.responses.GetQuestDefinitionsResponseItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class QuestService {
    private QuestCategoryRepository questCategoryRepository;
    private QuestDefinitionRepository questDefinitionRepository;

    @Autowired
    public QuestService(QuestCategoryRepository questCategoryRepository,
                        QuestDefinitionRepository questDefinitionRepository) {
        this.questCategoryRepository = questCategoryRepository;
        this.questDefinitionRepository = questDefinitionRepository;
    }

    public GetQuestCategoriesResponse getQuestCategories() {
        ArrayList<GetQuestCategoriesResponseItem> categories = new ArrayList<>();

        for (QuestCategory questCategoryEntity : questCategoryRepository.findAll()) {
            GetQuestCategoriesResponseItem questCategory = new GetQuestCategoriesResponseItem();
            questCategory.setId(questCategoryEntity.getId());
            questCategory.setGenerationHourOfDay(questCategoryEntity.getGenerationHourOfDay());
            questCategory.setGenerationDayOfWeek(questCategoryEntity.getGenerationDayOfWeek());
            categories.add(questCategory);
        }

        GetQuestCategoriesResponse response = new GetQuestCategoriesResponse();
        response.setCategories(categories);
        return response;
    }

    public void putQuestCategories(PutQuestCategoriesRequest request) {
        // Prepare collections.
        HashMap<String, QuestCategory> categories = new HashMap<>();

        ArrayList<QuestCategory> categoriesToSave = new ArrayList<>();
        ArrayList<QuestCategory> categoriesToDelete = new ArrayList<>();

        // Query current state from database.
        for (QuestCategory category : questCategoryRepository.findAll()) {
            categories.put(category.getId(), category);
        }

        // Collect requested quest categories.
        for (PutQuestCategoriesRequestItem category : request.getCategories()) {
            QuestCategory categoryEntity = categories.get(category.getId());

            if (categoryEntity == null) {
                categoryEntity = new QuestCategory();
                categoryEntity.setId(category.getId());
                categories.put(category.getId(), categoryEntity);
            }

            categoryEntity.setGenerationHourOfDay(category.getGenerationHourOfDay());
            categoryEntity.setGenerationDayOfWeek(category.getGenerationDayOfWeek());

            categoriesToSave.add(categoryEntity);
        }

        // Find categories to remove.
        for (Map.Entry<String, QuestCategory> category : categories.entrySet()) {
            if (request.getCategories().stream().noneMatch(c -> c.getId().equals(category.getKey()))) {
                categoriesToDelete.add(category.getValue());
            }
        }

        // Apply changes.
        questCategoryRepository.saveAll(categoriesToSave);
        questCategoryRepository.deleteAll(categoriesToDelete);
    }

    public GetQuestDefinitionsResponse getQuestDefinitions() {
        ArrayList<GetQuestDefinitionsResponseItem> questDefinitions = new ArrayList<>();

        for (QuestDefinition questDefinitionEntity : questDefinitionRepository.findAll()) {
            GetQuestDefinitionsResponseItem questDefinition = new GetQuestDefinitionsResponseItem();
            questDefinition.setId(questDefinitionEntity.getId());
            questDefinition.setCategory(questDefinitionEntity.getCategory().getId());
            questDefinition.setRequiredProgress(questDefinitionEntity.getRequiredProgress());
            questDefinition.setRewardItemDefinitionId(questDefinitionEntity.getRewardItemDefinitionId());
            questDefinition.setRewardItemCount(questDefinitionEntity.getRewardItemCount());
            questDefinitions.add(questDefinition);
        }

        GetQuestDefinitionsResponse response = new GetQuestDefinitionsResponse();
        response.setQuestDefinitions(questDefinitions);
        return response;
    }

    public void putQuestDefinitions(PutQuestDefinitionsRequest request) throws ApiException {
        // Prepare collections.
        HashMap<String, QuestCategory> categories = new HashMap<>();
        HashMap<String, QuestDefinition> questDefinitions = new HashMap<>();

        ArrayList<QuestDefinition> definitionsToSave = new ArrayList<>();
        ArrayList<QuestDefinition> definitionsToDelete = new ArrayList<>();

        // Query current state from database.
        for (QuestCategory category : questCategoryRepository.findAll()) {
            categories.put(category.getId(), category);
        }

        for (QuestDefinition questDefinition : questDefinitionRepository.findAll()) {
            questDefinitions.put(questDefinition.getId(), questDefinition);
        }

        // Collect requested quest definitions.
        for (PutQuestDefinitionsRequestItem questDefinition : request.getQuestDefinitions()) {
            QuestDefinition questDefinitionEntity = questDefinitions.get(questDefinition.getId());

            if (questDefinitionEntity == null) {
                questDefinitionEntity = new QuestDefinition();
                questDefinitionEntity.setId(questDefinition.getId());
                questDefinitions.put(questDefinition.getId(), questDefinitionEntity);
            }

            QuestCategory category = categories.get(questDefinition.getCategory());

            if (category == null) {
                throw new ApiException(ApiErrors.UNKNOWN_QUEST_CATEGORY_CODE,
                        ApiErrors.UNKNOWN_QUEST_CATEGORY_MESSAGE + questDefinition.getCategory());
            }

            questDefinitionEntity.setCategory(category);
            questDefinitionEntity.setRequiredProgress(questDefinition.getRequiredProgress());
            questDefinitionEntity.setRewardItemDefinitionId(questDefinition.getRewardItemDefinitionId());
            questDefinitionEntity.setRewardItemCount(questDefinition.getRewardItemCount());

            definitionsToSave.add(questDefinitionEntity);
        }

        // Find definitions to remove.
        for (Map.Entry<String, QuestDefinition> questDefinition : questDefinitions.entrySet()) {
            if (request.getQuestDefinitions().stream().noneMatch(q -> q.getId().equals(questDefinition.getKey()))) {
                definitionsToDelete.add(questDefinition.getValue());
            }
        }

        // Apply changes.
        questDefinitionRepository.saveAll(definitionsToSave);
        questDefinitionRepository.deleteAll(definitionsToDelete);
    }
}
