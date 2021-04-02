package de.opengamebackend.quests.controller;

import com.google.common.base.Strings;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestService {
    private QuestCategoryRepository questCategoryRepository;
    private QuestDefinitionRepository questDefinitionRepository;
    private PlayerQuestRepository playerQuestRepository;

    private CollectionService collectionService;

    @Autowired
    public QuestService(QuestCategoryRepository questCategoryRepository,
                        QuestDefinitionRepository questDefinitionRepository,
                        PlayerQuestRepository playerQuestRepository,
                        CollectionService collectionService) {
        this.questCategoryRepository = questCategoryRepository;
        this.questDefinitionRepository = questDefinitionRepository;
        this.playerQuestRepository = playerQuestRepository;

        this.collectionService = collectionService;
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

    public CreateQuestsResponse createQuests(String playerId) throws ApiException {
        if (Strings.isNullOrEmpty(playerId)) {
            throw new ApiException(ApiErrors.MISSING_PLAYER_ID_CODE, ApiErrors.MISSING_PLAYER_ID_MESSAGE);
        }

        CreateQuestsResponse response = new CreateQuestsResponse();

        // Get player quests.
        List<PlayerQuest> playerQuests = playerQuestRepository.findByPlayerId(playerId);

        // Add incomplete quests to response.
        List<PlayerQuest> incompleteQuests = playerQuests.stream()
                .filter(q -> q.getCompletedAt() == null)
                .collect(Collectors.toList());

        for (PlayerQuest incompleteQuest : incompleteQuests) {
            CreateQuestsResponseItem responseItem = mapToCreateQuestsResponseItem(incompleteQuest, false);
            response.getQuests().add(responseItem);
        }

        // For each quest category, check whether we need to generate a new quest.
        OffsetDateTime now = OffsetDateTime.now();

        for (QuestCategory questCategory : questCategoryRepository.findAll()) {
            // Find latest generated quest.
            PlayerQuest latestGeneratedQuest = playerQuests.stream()
                    .filter(q -> q.getDefinition().getCategory().getId().equals(questCategory.getId()))
                    .sorted(Comparator.comparing(PlayerQuest::getGeneratedAt))
                    .reduce((first, second) -> second)
                    .orElse(null);

            boolean hasNoQuests = latestGeneratedQuest == null;
            boolean isDailyQuest = questCategory.getGenerationDayOfWeek() == null;
            boolean isWeeklyQuest = !isDailyQuest;
            long daysSinceLastGeneration = hasNoQuests ? 0 : ChronoUnit.DAYS.between(latestGeneratedQuest.getGeneratedAt(), now);
            long weeksSinceLastGeneration = hasNoQuests ? 0 : ChronoUnit.WEEKS.between(latestGeneratedQuest.getGeneratedAt(), now);

            if (hasNoQuests
                    || (isDailyQuest && daysSinceLastGeneration > 0)
                    || (isWeeklyQuest && weeksSinceLastGeneration > 0)) {

                // Get available quest definitions.
                List<QuestDefinition> questDefinitions = questDefinitionRepository.findByCategory(questCategory);
                Random random = new Random();
                QuestDefinition questDefinition = questDefinitions.get(random.nextInt(questDefinitions.size()));

                // Generate new quest.
                PlayerQuest newPlayerQuest = new PlayerQuest();
                newPlayerQuest.setDefinition(questDefinition);
                newPlayerQuest.setPlayerId(playerId);
                newPlayerQuest.setGeneratedAt(now);

                playerQuestRepository.save(newPlayerQuest);

                // Add to response.
                CreateQuestsResponseItem responseItem = mapToCreateQuestsResponseItem(newPlayerQuest, true);
                response.getQuests().add(responseItem);
            }
        }

        return response;
    }

    public GetPlayerQuestsResponse getPlayerQuests(String playerId) {
        ArrayList<GetPlayerQuestsResponseItem> playerQuests = new ArrayList<>();

        for (PlayerQuest playerQuestEntity : playerQuestRepository.findByPlayerId(playerId)) {
            GetPlayerQuestsResponseItem playerQuest = new GetPlayerQuestsResponseItem();

            playerQuest.setId(playerQuestEntity.getId());
            playerQuest.setQuestCategoryId(playerQuestEntity.getDefinition().getCategory().getId());
            playerQuest.setQuestDefinitionId(playerQuestEntity.getDefinition().getId());
            playerQuest.setRequiredProgress(playerQuestEntity.getDefinition().getRequiredProgress());
            playerQuest.setCurrentProgress(playerQuestEntity.getCurrentProgress());
            playerQuest.setRewardItemDefinitionId(playerQuestEntity.getDefinition().getRewardItemDefinitionId());
            playerQuest.setRewardItemCount(playerQuestEntity.getDefinition().getRewardItemCount());
            playerQuest.setGeneratedAt(playerQuestEntity.getGeneratedAt());
            playerQuest.setCompletedAt(playerQuestEntity.getCompletedAt());

            playerQuests.add(playerQuest);
        }

        GetPlayerQuestsResponse response = new GetPlayerQuestsResponse();
        response.setQuests(playerQuests);
        return response;
    }

    public void increaseQuestProgress(String playerId, String questDefinitionId, IncreaseQuestProgressRequest request)
            throws ApiException {
        // Find quest.
        if (Strings.isNullOrEmpty(playerId)) {
            throw new ApiException(ApiErrors.MISSING_PLAYER_ID_CODE, ApiErrors.MISSING_PLAYER_ID_MESSAGE);
        }

        QuestDefinition questDefinition = questDefinitionRepository.findById(questDefinitionId).orElse(null);

        if (questDefinition == null) {
            throw new ApiException(ApiErrors.UNKNOWN_QUEST_DEFINITION_CODE,
                    ApiErrors.UNKNOWN_QUEST_DEFINITION_MESSAGE + questDefinitionId);
        }

        List<PlayerQuest> playerQuests = playerQuestRepository.findByPlayerIdAndQuestDefinition(playerId, questDefinition);
        PlayerQuest playerQuest = playerQuests.stream()
                .filter(q -> q.getCompletedAt() == null)
                .findFirst()
                .orElse(null);

        if (playerQuest == null) {
            return;
        }

        // Update progress.
        int oldProgress = playerQuest.getCurrentProgress();
        int newProgress = Math.min(oldProgress + request.getProgressMade(), questDefinition.getRequiredProgress());

        playerQuest.setCurrentProgress(newProgress);
        playerQuestRepository.save(playerQuest);
    }

    public FinishQuestResponse finishQuest(String playerId, String questDefinitionId) throws ApiException {
        // Find quest.
        if (Strings.isNullOrEmpty(playerId)) {
            throw new ApiException(ApiErrors.MISSING_PLAYER_ID_CODE, ApiErrors.MISSING_PLAYER_ID_MESSAGE);
        }

        QuestDefinition questDefinition = questDefinitionRepository.findById(questDefinitionId).orElse(null);

        if (questDefinition == null) {
            throw new ApiException(ApiErrors.UNKNOWN_QUEST_DEFINITION_CODE,
                    ApiErrors.UNKNOWN_QUEST_DEFINITION_MESSAGE + questDefinitionId);
        }

        List<PlayerQuest> playerQuests = playerQuestRepository.findByPlayerIdAndQuestDefinition(playerId, questDefinition);
        PlayerQuest playerQuest = playerQuests.stream()
                .filter(q -> q.getCompletedAt() == null)
                .findFirst()
                .orElse(null);

        if (playerQuest == null) {
            throw new ApiException(ApiErrors.QUEST_NOT_FOUND_CODE, ApiErrors.QUEST_NOT_FOUND_MESSAGE);
        }

        // Check progress.
        if (playerQuest.getCurrentProgress() < questDefinition.getRequiredProgress()) {
            throw new ApiException(ApiErrors.INSUFFICIENT_QUEST_PROGRESS_CODE,
                    ApiErrors.INSUFFICIENT_QUEST_PROGRESS_MESSAGE);
        }

        // Grant rewards.
        AddCollectionItemsRequest request = new AddCollectionItemsRequest();
        request.setItemDefinitionId(questDefinition.getRewardItemDefinitionId());
        request.setItemCount(questDefinition.getRewardItemCount());

        collectionService.addCollectionItems(playerId, request);

        // Mark completed.
        playerQuest.setCompletedAt(OffsetDateTime.now());
        playerQuestRepository.save(playerQuest);

        // Return response.
        FinishQuestResponse response = new FinishQuestResponse();
        response.setRewardItemDefinitionId(questDefinition.getRewardItemDefinitionId());
        response.setRewardItemCount(questDefinition.getRewardItemCount());
        return response;
    }

    private CreateQuestsResponseItem mapToCreateQuestsResponseItem(PlayerQuest quest, boolean isNewQuest) {
        CreateQuestsResponseItem responseItem = new CreateQuestsResponseItem();
        responseItem.setId(quest.getId());
        responseItem.setQuestCategoryId(quest.getDefinition().getCategory().getId());
        responseItem.setQuestDefinitionId(quest.getDefinition().getId());
        responseItem.setCurrentProgress(quest.getCurrentProgress());
        responseItem.setRequiredProgress(quest.getDefinition().getRequiredProgress());
        responseItem.setRewardItemDefinitionId(quest.getDefinition().getRewardItemDefinitionId());
        responseItem.setRewardItemCount(quest.getDefinition().getRewardItemCount());
        responseItem.setGeneratedAt(quest.getGeneratedAt());
        responseItem.setNewQuest(isNewQuest);
        return responseItem;
    }
}
