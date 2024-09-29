package validator;

import com.fasterxml.jackson.databind.JsonNode;

public class ScoreValueValidator implements Validator {
    @Override
    public String validate(JsonNode requestNode) {
        JsonNode scoreNode = requestNode.findPath("score");
        return scoreNode.asLong() < 0 ? "Score must be non-negative." : null;
    }
}