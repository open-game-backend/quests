package de.opengamebackend.quests.controller;

import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import de.opengamebackend.net.HttpHeader;
import de.opengamebackend.quests.model.requests.IncreaseQuestProgressRequest;
import de.opengamebackend.quests.model.requests.PutQuestCategoriesRequest;
import de.opengamebackend.quests.model.requests.PutQuestDefinitionsRequest;
import de.opengamebackend.quests.model.responses.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class QuestController {
    private final QuestService questService;

    public QuestController(QuestService questService) {
        this.questService = questService;
    }

    @GetMapping("/admin/questcategories")
    @Operation(summary = "Gets all available quest categories.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Quest categories fetched.")
    })
    public ResponseEntity<GetQuestCategoriesResponse> getQuestCategories() {
        GetQuestCategoriesResponse response = questService.getQuestCategories();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/admin/questcategories")
    @Operation(summary = "Sets the definitions of all quest categories.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Quest categories updated.")
    })
    public ResponseEntity<Void> putQuestCategories(@RequestBody PutQuestCategoriesRequest request) {
        questService.putQuestCategories(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/admin/questdefinitions")
    @Operation(summary = "Gets all available quest definitions.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Quest definitions fetched.")
    })
    public ResponseEntity<GetQuestDefinitionsResponse> getQuestDefinitions() {
        GetQuestDefinitionsResponse response = questService.getQuestDefinitions();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/admin/questdefinitions")
    @Operation(summary = "Sets the definitions of all quests.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Quest definitions updated."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error " + ApiErrors.UNKNOWN_QUEST_CATEGORY_CODE + ": " + ApiErrors.UNKNOWN_QUEST_CATEGORY_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<Void> putQuestDefinitions(@RequestBody PutQuestDefinitionsRequest request) throws ApiException {
        questService.putQuestDefinitions(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/client/createquests")
    @Operation(summary = "Generates new daily or weekly quests, if possible, then returns all available quests.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Quests fetched."),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_PLAYER_ID_CODE + ": " + ApiErrors.MISSING_PLAYER_ID_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<CreateQuestsResponse> createQuests(@RequestHeader(HttpHeader.PLAYER_ID) String playerId)
            throws ApiException {
        CreateQuestsResponse response = questService.createQuests(playerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/admin/playerquests")
    @Operation(summary = "Gets all quests of the specified player, both finished and unfinished.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Player quests fetched.")
    })
    public ResponseEntity<GetPlayerQuestsResponse> getPlayerQuests(@PathVariable String playerId) {
        GetPlayerQuestsResponse response = questService.getPlayerQuests(playerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/increasequestprogress")
    @Operation(summary = "Increases the progress of the active quest of the specified definition for the passed player.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Quest progress increased."),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_PLAYER_ID_CODE + ": " + ApiErrors.MISSING_PLAYER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.UNKNOWN_QUEST_DEFINITION_CODE + ": " + ApiErrors.UNKNOWN_QUEST_DEFINITION_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<Void> increaseQuestProgress(@PathVariable String playerId,
                                                      @PathVariable String questDefinitionId,
                                                      @RequestBody IncreaseQuestProgressRequest request)
            throws ApiException {
        questService.increaseQuestProgress(playerId, questDefinitionId, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/client/finishquest")
    @Operation(summary = "Finishes the active quest of the specified definition and claims its rewards.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Quest finished."),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_PLAYER_ID_CODE + ": " + ApiErrors.MISSING_PLAYER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.UNKNOWN_QUEST_DEFINITION_CODE + ": " + ApiErrors.UNKNOWN_QUEST_DEFINITION_MESSAGE + "<br />" +
                            "Error " + ApiErrors.QUEST_NOT_FOUND_CODE + ": " + ApiErrors.QUEST_NOT_FOUND_MESSAGE  + "<br />" +
                            "Error " + ApiErrors.INSUFFICIENT_QUEST_PROGRESS_CODE + ": " + ApiErrors.INSUFFICIENT_QUEST_PROGRESS_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<FinishQuestResponse> finishQuest(@RequestHeader(HttpHeader.PLAYER_ID) String playerId,
                                                           @PathVariable String questDefinitionId)
            throws ApiException {
        FinishQuestResponse response = questService.finishQuest(playerId, questDefinitionId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
