package validator;

import com.fasterxml.jackson.databind.JsonNode;

public class ScoreTypeValidator implements Validator {
    @Override
    public String validate(JsonNode requestNode) {
        JsonNode scoreNode = requestNode.findPath("score");
        return !scoreNode.isNumber() ? "Score must be a valid number." : null;
    }
}
