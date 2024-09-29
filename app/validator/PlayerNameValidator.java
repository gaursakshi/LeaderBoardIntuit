package validator;

import com.fasterxml.jackson.databind.JsonNode;

public class PlayerNameValidator implements Validator {
    @Override
    public String validate(JsonNode requestNode) {
        JsonNode playerNameNode = requestNode.findPath("playerName");
        return playerNameNode.isMissingNode() ? "Player name is required." : null;
    }
}
