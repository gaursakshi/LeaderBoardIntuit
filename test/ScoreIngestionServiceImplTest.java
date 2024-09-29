import exceptions.DatabaseStorageException;
import exceptions.LeaderboardUpdateFailureException;
import models.PlayerScore;
import models.Response;
import services.Leaderboards.LeaderBoard;
import services.Scores.ScoreIngestionServiceImpl;
import views.PlayerScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScoreIngestionServiceImplTest {

    private PlayerScoreRepository scoreRepository;
    private ScoreIngestionServiceImpl scoreIngestionService;
    private LeaderBoard leaderBoard;

    @BeforeEach
    void setUp() {
        scoreRepository = mock(PlayerScoreRepository.class);
        scoreIngestionService = new ScoreIngestionServiceImpl(scoreRepository);
        leaderBoard = mock(LeaderBoard.class);
        scoreIngestionService.registerLeaderBoard(leaderBoard);
    }

    @Test
    void testPublish_Success() throws DatabaseStorageException, LeaderboardUpdateFailureException {
        PlayerScore newScore = new PlayerScore("player1", 100L, "Player One");

        when(scoreRepository.findById(PlayerScore.class, createQueryParams("player1")))
                .thenReturn(Optional.empty());

        // Simulating save behavior
        doNothing().when(scoreRepository).save(newScore, Optional.empty());

        Response response = scoreIngestionService.publish(newScore);

        assertEquals("success", response.getStatus());
        assertEquals("User score ingested successfully", response.getMessage());
        assertEquals("player1", response.getPlayerId());

        verify(leaderBoard).publish(newScore);
    }

    @Test
    void testPublish_ExistingHigherScore() throws DatabaseStorageException,LeaderboardUpdateFailureException {
        PlayerScore existingScore = new PlayerScore("player1", 150L, "Player One");
        PlayerScore newScore = new PlayerScore("player1", 100L, "Player One");

        when(scoreRepository.findById(PlayerScore.class, createQueryParams("player1")))
                .thenReturn(Optional.of(existingScore));

        Response response = scoreIngestionService.publish(newScore);

        assertEquals("failure", response.getStatus());
        assertEquals("User score insertion failed", response.getMessage());
        assertEquals("player1", response.getPlayerId());

        verify(scoreRepository, never()).save(any(), any());
        verify(leaderBoard, never()).publish(any());
    }

    @Test
    void testPublish_DatabaseStorageException() throws DatabaseStorageException,LeaderboardUpdateFailureException {
        PlayerScore newScore = new PlayerScore("player1", 100L, "Player One");

        when(scoreRepository.findById(PlayerScore.class, createQueryParams("player1")))
                .thenReturn(Optional.empty());

        doThrow(new DatabaseStorageException("Database error")).when(scoreRepository).save(any(), any());

        Response response = scoreIngestionService.publish(newScore);

        assertEquals("failure", response.getStatus());
        assertEquals("User score insertion failed", response.getMessage());
        assertEquals("player1", response.getPlayerId());

        verify(leaderBoard, never()).publish(any());
    }

    @Test
    void testPublish_LeaderboardUpdateFailureException() throws DatabaseStorageException, LeaderboardUpdateFailureException {
        PlayerScore newScore = new PlayerScore("player1", 100L, "Player One");

        when(scoreRepository.findById(PlayerScore.class, createQueryParams("player1")))
                .thenReturn(Optional.empty());

        doNothing().when(scoreRepository).save(newScore, Optional.empty());
        doThrow(new LeaderboardUpdateFailureException("Update failed")).when(leaderBoard).publish(newScore);

        Response response = scoreIngestionService.publish(newScore);

        assertEquals("failure", response.getStatus());
        assertEquals("User score insertion failed", response.getMessage());
        assertEquals("player1", response.getPlayerId());
    }

    private Map<String, Object> createQueryParams(String playerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("player_id", playerId);
        return params;
    }
}
