package validator;

import com.fasterxml.jackson.databind.JsonNode;

public interface Validator {
    String validate(JsonNode requestNode);
}