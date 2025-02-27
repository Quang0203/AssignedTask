package cviettel.loginservice.exception.handler;

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
public class UnauthorizedException extends AbstractThrowableProblem {

    /**
     * The Constant serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private final String errorKey;

    private final Object[] errorParams;

    public UnauthorizedException(MessageCode msgCode) {
        this(Labels.getLabels(msgCode.getKey()), msgCode.name(), msgCode.getKey());
    }

    /**
     * Contructor 3 tham số để gán giá trị vào label
     *
     * @param defaultMessage
     * @param errorCode
     * @param errorKey
     */
    public UnauthorizedException(String defaultMessage, String errorCode, String errorKey) {
        this(ApiConstants.ErrorType.DEFAULT_TYPE, defaultMessage, errorCode, errorKey, new Object[0]);
    }

    /**
     * Contructor 5 tham số để gán giá trị vào label
     *
     * @param type
     * @param defaultMessage
     * @param errorCode
     * @param errorKey
     * @param errorParams
     */
    public UnauthorizedException(URI type, String defaultMessage, String errorCode, String errorKey,
                                 Object[] errorParams) {
        super(type, defaultMessage, Status.UNAUTHORIZED, null, null, null,
                getAlertParameters(defaultMessage, errorCode, errorKey, errorParams));

        this.errorKey = errorKey;
        this.errorParams = errorParams;
    }

    /**
     * Contructor 4 tham số để gán giá trị vào label
     *
     * @param message
     * @param errorCode
     * @param errorKey
     * @param errorParams
     */
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

