package services.Cache;

import exceptions.CacheInitializationException;
import exceptions.CacheUpdateFailureException;
import models.PlayerScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PlayerScoreCache implements CacheServices {

    private int topN;
    private PriorityQueue<PlayerScore> minHeap;
    private Map<String, PlayerScore> playerToScore;
    private static final Logger logger = LoggerFactory.getLogger(PlayerScoreCache.class);

    @Override
    public void initialize(int topN, List<PlayerScore> dataSet) throws CacheInitializationException {
        this.topN = topN;
        minHeap = new PriorityQueue<>();
        playerToScore = new HashMap<>();
        try {
            for (PlayerScore score : dataSet) {
                addScoreToCache(score);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize cache - {}", e.getMessage());
            throw new CacheInitializationException("Failed to initialize cache");
        }
    }

    @Override
    public void addToCache(PlayerScore score) throws CacheUpdateFailureException {
        try {
            if (playerToScore.containsKey(score.getPlayerId())) {
                updateExistingScore(score);
            } else {
                addNewScore(score);
            }
        } catch (Exception e) {
            logger.error("Failed to update cache - {}", e.getMessage());
            throw new CacheUpdateFailureException("Failed to update cache");
        }
    }

    @Override
    public List<PlayerScore> getTopNPlayers() {
        List<PlayerScore> result = new ArrayList<>(minHeap);
        result.sort(Collections.reverseOrder());
        return result;
    }

    private void addScoreToCache(PlayerScore score) {
        if (minHeap.size() < topN) {
            minHeap.add(score);
            playerToScore.put(score.getPlayerId(), score);
        } else if (score.getScore() > minHeap.peek().getScore()) {
            replaceLowestScore(score);
        }
    }

    private void updateExistingScore(PlayerScore score) {
        PlayerScore existingScore = playerToScore.get(score.getPlayerId());
        if (existingScore.getScore() < score.getScore()) {
            logger.info("Updating {}'s score to {}", existingScore.getPlayerId(), score.getScore());
            minHeap.remove(existingScore);
            minHeap.add(score);
            playerToScore.put(score.getPlayerId(), score);
        }
    }

    private void addNewScore(PlayerScore score) {
        if (minHeap.size() < topN) {
            minHeap.add(score);
            playerToScore.put(score.getPlayerId(), score);
        } else {
            replaceLowestScore(score);
        }
    }

    private void replaceLowestScore(PlayerScore score) {
        PlayerScore removedScore = minHeap.poll();
        minHeap.add(score);
        playerToScore.remove(removedScore.getPlayerId());
        playerToScore.put(score.getPlayerId(), score);
    }
}
