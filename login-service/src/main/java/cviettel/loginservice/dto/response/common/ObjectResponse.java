package cviettel.loginservice.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectResponse<T> {

    private int code;
    private String message;
    private Instant timestamp;
    private T data;

    public ObjectResponse(int code, String message, Instant timestamp, T data) {
        this.data = data;
        this.message = message;
        this.timestamp = timestamp;
        this.code = code;
    }

    public ObjectResponse(int code, String message, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
