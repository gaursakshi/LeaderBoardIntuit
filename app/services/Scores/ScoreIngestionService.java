package services.Scores;


import com.fasterxml.jackson.core.JsonProcessingException;
import exceptions.DatabaseStorageException;
import exceptions.LeaderboardUpdateFailureException;
import models.PlayerScore;
import models.Response;

public interface ScoreIngestionService {
	Response publish(PlayerScore newScore) throws LeaderboardUpdateFailureException, DatabaseStorageException, JsonProcessingException;
}
