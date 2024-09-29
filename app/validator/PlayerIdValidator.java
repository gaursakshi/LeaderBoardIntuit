package validator;

import com.fasterxml.jackson.databind.JsonNode;

public class PlayerIdValidator implements Validator {
    @Override
    public String validate(JsonNode requestNode) {
        JsonNode playerIdNode = requestNode.findPath("playerId");
        return playerIdNode.isMissingNode() || playerIdNode.asText().isEmpty() ? "Player ID is required." : null;
    }
}
