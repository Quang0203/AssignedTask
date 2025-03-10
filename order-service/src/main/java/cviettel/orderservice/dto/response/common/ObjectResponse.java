package cviettel.orderservice.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectResponse<T, U> {

    private String code;
    private String message;
    private U timestamp;
    private T data;

    public ObjectResponse(String code, String message, U timestamp, T data) {
        this.data = data;
        this.message = message;
        this.timestamp = timestamp;
        this.code = code;
    }

    public ObjectResponse(String code, String message, U timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }
}
