package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.PlayerScore;
import models.Response;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.Scores.ScoreIngestionService;
import com.google.inject.Inject;
import validator.*;

import java.util.Arrays;

public class GameController extends Controller {
    private final ObjectMapper objectMapper;
    private final ScoreIngestionService scoreIngestor;
    private final RequestValidator requestValidator;

    @Inject
    public GameController(ObjectMapper objectMapper, ScoreIngestionService scoreIngestor) {
        this.objectMapper = objectMapper;
        this.scoreIngestor = scoreIngestor;
        this.requestValidator = new RequestValidator(Arrays.asList(
                new PlayerNameValidator(),
                new PlayerIdValidator(),
                new ScoreTypeValidator(),
                new ScoreValueValidator()
        ));
    }

    public Result postScore(Http.Request request) {
        JsonNode requestNode = request.body().asJson();
        String errorMessage = requestValidator.validate(requestNode);
        if (errorMessage != null) {
            return badRequest(errorMessage);
        }
        try {
            PlayerScore newScore = objectMapper.treeToValue(requestNode, PlayerScore.class);
            Response response = scoreIngestor.publish(newScore);
            return handleResponse(response);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    private boolean isInvalidRequest(JsonNode requestNode) {
        if (requestNode == null || requestNode.isEmpty()) {
            return true;
        }
        JsonNode playerNameNode = requestNode.findPath("playerName");
        JsonNode scoreNode = requestNode.findPath("score");
        return playerNameNode.isMissingNode() ||
                !scoreNode.isNumber() ||
                scoreNode.asLong() < 0; // Check if score is negative
    }

    private Result handleResponse(Response response) {
        if ("failure".equals(response.getStatus())) {
            return badRequest(Json.toJson(response));
        }
        return ok(Json.toJson(response));
    }

    private Result handleError(Exception e) {
        System.out.println("Leaderboard Update failed - " + e.getMessage());
        return internalServerError(e.getMessage());
    }
}
