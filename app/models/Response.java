package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response {

    private String message ;

    private String playerId;

    private String status;

    private String reason;
}
