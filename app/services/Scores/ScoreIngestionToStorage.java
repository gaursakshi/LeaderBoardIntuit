package services.Scores;


import exceptions.DatabaseStorageException;
import models.PlayerScore;

public interface ScoreIngestionToStorage {
	public String publishToDatabaseStore(PlayerScore newScore) throws DatabaseStorageException;
}
