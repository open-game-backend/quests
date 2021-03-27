package de.opengamebackend.quests.controller;

import de.opengamebackend.quests.model.entities.QuestCategory;
import de.opengamebackend.quests.model.repositories.QuestCategoryRepository;
import de.opengamebackend.quests.model.requests.PutQuestCategoriesRequest;
import de.opengamebackend.quests.model.requests.PutQuestCategoriesRequestItem;
import de.opengamebackend.quests.model.responses.GetQuestCategoriesReponseItem;
import de.opengamebackend.quests.model.responses.GetQuestCategoriesResponse;
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

    @Autowired
    public QuestService(QuestCategoryRepository questCategoryRepository) {
        this.questCategoryRepository = questCategoryRepository;
    }

    public GetQuestCategoriesResponse getQuestCategories() {
        ArrayList<GetQuestCategoriesReponseItem> categories = new ArrayList<>();

        for (QuestCategory questCategoryEntity : questCategoryRepository.findAll()) {
            GetQuestCategoriesReponseItem questCategory = new GetQuestCategoriesReponseItem();
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
}
