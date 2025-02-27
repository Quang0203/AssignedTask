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

/**
 * Handler BadRequest Exception
 *
 * @author minhquang
 * @version 1.0
 * @since 2024-11-25
 */
@Getter
public class BadRequestAlertException extends AbstractThrowableProblem {

    /**
     * The Constant serialVersionUID
     */
    private static final long serialVersionUID = -6642851821916766469L;

    private final String errorKey;

    private final Object[] errorParams;


    /**
     * Contructor 1 tham số để gán giá trị vào label
     *
     * @param errorCode
     */
    public BadRequestAlertException(MessageCode errorCode) {
        this(Labels.getLabels(errorCode.getKey()), errorCode.name(), errorCode.getKey());
    }

    /**
     * Contructor 3 tham số để gán giá trị vào label
     *
     * @param defaultMessage
     * @param errorCode
     * @param errorKey
     */
    public BadRequestAlertException(String defaultMessage, String errorCode, String errorKey) {
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
    public BadRequestAlertException(URI type, String defaultMessage, String errorCode, String errorKey,
                                    Object[] errorParams) {
        super(type, defaultMessage, Status.BAD_REQUEST, null, null, null,
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
