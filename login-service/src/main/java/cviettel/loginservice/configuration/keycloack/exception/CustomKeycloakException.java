package cviettel.loginservice.configuration.keycloack.exception;

import cviettel.loginservice.configuration.message.Labels;
import cviettel.loginservice.constants.ApiConstants;
import cviettel.loginservice.enums.MessageCode;
import lombok.Getter;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class CustomKeycloakException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    private final String errorKey;
    private final Object[] errorParams;

    /**
     * Contructor 1 tham số để gán giá trị vào label
     *
     * @param errorCode
     */
    public CustomKeycloakException(MessageCode errorCode) {
        this(Labels.getLabels(errorCode.getKey()), errorCode.name(), errorCode.getKey());
    }

    /**
     * Constructor 3 tham số, sử dụng error type mặc định cho Keycloak.
     *
     * @param defaultMessage Thông báo lỗi
     * @param errorCode      Mã lỗi
     * @param errorKey       Key lỗi
     */
    public CustomKeycloakException(String defaultMessage, String errorCode, String errorKey) {
        this(ApiConstants.ErrorType.DEFAULT_TYPE, defaultMessage, errorCode, errorKey, new Object[0]);
    }

    /**
     * Constructor 4 tham số, cho phép truyền thêm các tham số lỗi.
     *
     * @param defaultMessage Thông báo lỗi
     * @param errorCode      Mã lỗi
     * @param errorKey       Key lỗi
     * @param errorParams    Các tham số bổ sung
     */
    public CustomKeycloakException(String defaultMessage, String errorCode, String errorKey, Object[] errorParams) {
        this(ApiConstants.ErrorType.DEFAULT_TYPE, defaultMessage, errorCode, errorKey, errorParams);
    }

    /**
     * Constructor 5 tham số để cấu hình đầy đủ cho lỗi Keycloak.
     *
     * @param type           URI của error type
     * @param defaultMessage Thông báo lỗi
     * @param errorCode      Mã lỗi
     * @param errorKey       Key lỗi
     * @param errorParams    Các tham số bổ sung
     */
    public CustomKeycloakException(URI type, String defaultMessage, String errorCode, String errorKey, Object[] errorParams) {
        super(type, defaultMessage, Status.INTERNAL_SERVER_ERROR, null, null, null,
                getAlertParameters(defaultMessage, errorCode, errorKey, errorParams));
        this.errorKey = errorKey;
        this.errorParams = errorParams;
    }

    private static Map<String, Object> getAlertParameters(String message, String errorCode, String errorKey,
                                                          Object[] errorParams) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ApiConstants.ErrorKey.MESSAGE, message);
        parameters.put(ApiConstants.ErrorKey.ERROR_CODE, errorCode);
        parameters.put(ApiConstants.ErrorKey.ERROR_KEY, errorKey);
        parameters.put(ApiConstants.ErrorKey.PARAMS, Arrays.asList(errorParams));
        return parameters;
    }
}
