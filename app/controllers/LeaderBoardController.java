package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import exceptions.CacheInitializationException;
import exceptions.LeaderboardNotInitializedException;
import exceptions.LeaderboardUpdateFailureException;
import models.PlayerScore;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.Leaderboards.LeaderBoardServiceImp;

import java.util.List;

/**
 * Handles leaderboard-related HTTP requests.
 */
public class LeaderBoardController extends Controller {

    private final LeaderBoardServiceImp leaderBoardService;

    @Inject
    public LeaderBoardController(LeaderBoardServiceImp leaderBoardService) {
        this.leaderBoardService = leaderBoardService;
    }


    /**
     * Creates a leaderboard of the specified size.
     *
     * @param boardSize the size of the leaderboard.
     * @return status message indicating success or failure.
     */
    public Result createLeaderBoard(int boardSize) {
        try {
            if (boardSize <= 0) {
                throw new IllegalArgumentException("Leaderboard size must be greater than zero");
            }
            leaderBoardService.createBoard(boardSize);
            return ok("Leaderboard created successfully");
        } catch (CacheInitializationException | LeaderboardNotInitializedException e) {
            return status(INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (IllegalArgumentException e) {
            return status(BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Gets the top N players from the leaderboard.
     *
     * @return JSON response with the top players or error status.
     */
    public Result getTopNPlayers() {
        try {
            List<PlayerScore> topPlayers = leaderBoardService.getTopNPlayers();
            return topPlayers.isEmpty() ? notFound(createEmptyLeaderboardResponse()) : ok(Json.toJson(topPlayers));
        } catch (LeaderboardNotInitializedException e) {
            return handleError(BAD_REQUEST, e);
        } catch (Exception e) {
            return handleError(INTERNAL_SERVER_ERROR, e);
        }
    }

    private ObjectNode createEmptyLeaderboardResponse() {
        ObjectNode response = Json.newObject();
        response.put("message", "The leaderboard is currently empty. No players have recorded scores yet.");
        return response;
    }

    private Result handleError(int statusCode, Exception e) {
        System.err.println("Error occurred: " + e.getMessage());
        return status(statusCode, e.getMessage());
    }

}
