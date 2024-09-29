package services.Scores;


import exceptions.DatabaseStorageException;
import exceptions.LeaderboardUpdateFailureException;
import models.PlayerScore;

import models.Response;
import services.Leaderboards.LeaderBoard;
import views.PlayerScoreRepository;

import javax.inject.Inject;
import java.util.*;

public class ScoreIngestionServiceImpl implements ScoreIngestionToLeaderBoards, ScoreIngestionToStorage, ScoreIngestionService {

    public static List<LeaderBoard> leaderBoards = new ArrayList<>();

    private final PlayerScoreRepository scoreRepository;

    @Inject
    public ScoreIngestionServiceImpl(PlayerScoreRepository playScoreRepository) {
        this.scoreRepository = playScoreRepository;
    }


    @Override
    public String publishToDatabaseStore(PlayerScore newScore) throws DatabaseStorageException {
        try {
            Map<String, Object> queryParamsMap = new HashMap<>();
            queryParamsMap.put("player_id", newScore.getPlayerId());
            Optional<PlayerScore> scoreAlreadyPresent = scoreRepository.findById(PlayerScore.class, queryParamsMap);
            if (scoreAlreadyPresent.isPresent() && scoreAlreadyPresent.get().getScore() >= newScore.getScore()) {
                System.out.println("Score already present");
                throw new DatabaseStorageException("Score already exists");
            }
            scoreRepository.save(newScore,scoreAlreadyPresent);
            return newScore.getPlayerId(); // Ensure this ID is assigned correctly after save
        } catch (Exception e) {
            throw new DatabaseStorageException("Could not publish data to  database: " + e.getMessage());
        }
    }

    @Override
    public void registerLeaderBoard(LeaderBoard leaderBoard) {
        leaderBoards.add(leaderBoard); // Add the leaderboard to the list of registered leaderboards
        System.out.println("Registered leaderboards: " + leaderBoards.size());
    }

    @Override
    public void publishToLeaderBoards(PlayerScore newScore) throws LeaderboardUpdateFailureException {
        for (LeaderBoard leaderBoard : leaderBoards) {
            leaderBoard.publish(newScore); // Publish the new score to each registered leaderboard
        }
    }


    @Override
    public Response publish(PlayerScore newScore) throws LeaderboardUpdateFailureException, DatabaseStorageException {
        String playerId;
        try {
            playerId = publishToDatabaseStore(newScore);
            publishToLeaderBoards(newScore);
            return Response.builder()
                    .message("User score ingested successfully")
                    .playerId(playerId)
                    .status("success")
                    .reason("Successfully published")
                    .build();
        } catch (LeaderboardUpdateFailureException | DatabaseStorageException e) {
            return Response.builder()
                    .message("User score insertion failed")
                    .playerId(newScore.getPlayerId())
                    .status("failure")
                    .reason(e.getMessage())
                    .build();
        }
    }

}
