package rabbitMq;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.PlayerScore;
import models.Response;
import play.libs.Json;
import services.Scores.ScoreIngestionService;
import validator.*;

import java.util.Arrays;

import static play.mvc.Results.badRequest;

public class PlayerScoreConsumer  extends RabbitMqClient{

    @Inject
    ScoreIngestionService scoreIngestor;
    private final RequestValidator requestValidator;

    public PlayerScoreConsumer() {
        this.requestValidator = new RequestValidator(Arrays.asList(
                new PlayerNameValidator(),
                new PlayerIdValidator(),
                new ScoreTypeValidator(),
                new ScoreValueValidator()
        ));
    }

    @Override
    public void processMessage(String message) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PlayerScore playerScore = objectMapper.readValue(message, PlayerScore.class);
            String errorMessage = requestValidator.validate(new ObjectMapper().readValue(message, JsonNode.class));
            if (errorMessage != null) {
                System.out.println(errorMessage);
                return;
            }
            Response response =scoreIngestor.publish(playerScore);
            System.out.println(response);
        }
        catch (Exception e){
            e.printStackTrace();

        }
    }

    public void startConsumer(){
        receiveMessage("player_score");
    }
}
