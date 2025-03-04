package cviettel.orderservice.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public U getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(U timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
