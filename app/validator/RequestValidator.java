package validator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class RequestValidator {
    private final List<Validator> validators;

    public RequestValidator(List<Validator> validators) {
        this.validators = validators;
    }

    public String validate(JsonNode requestNode) {
        for (Validator validator : validators) {
            String errorMessage = validator.validate(requestNode);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null; // No errors
    }
}