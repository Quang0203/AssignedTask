package cviettel.productservice.exception.handler;

import cviettel.productservice.configuration.message.Labels;
import cviettel.productservice.constants.ApiConstants;
import cviettel.productservice.enums.MessageCode;
import lombok.Getter;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler Not Found Exception
 *
 * @author minhquang
 * @version 1.0
 * @since 2024-11-25
 */
@Getter
public class NotFoundAlertException extends AbstractThrowableProblem {

  /**
   * The Constant serialVersionUID
   */
  private static final long serialVersionUID = -6642851821916766470L;

  private final String errorKey;

  private final Object[] errorParams;

  /**
   * Constructor 1 tham số để gán giá trị vào label
   *
   * @param errorCode MessageCode enum
   */
  public NotFoundAlertException(MessageCode errorCode) {
    this(Labels.getLabels(errorCode.getKey()), errorCode.name(), errorCode.getKey());
  }

  /**
   * Constructor 3 tham số để gán giá trị vào label
   *
   * @param defaultMessage Default error message
   * @param errorCode Error code
   * @param errorKey Error key
   */
  public NotFoundAlertException(String defaultMessage, String errorCode, String errorKey) {
    this(ApiConstants.ErrorType.DEFAULT_TYPE, defaultMessage, errorCode, errorKey, new Object[0]);
  }

  /**
   * Constructor 5 tham số để gán giá trị vào label
   *
   * @param type Error type URI
   * @param defaultMessage Default error message
   * @param errorCode Error code
   * @param errorKey Error key
   * @param errorParams Additional error parameters
   */
  public NotFoundAlertException(URI type, String defaultMessage, String errorCode, String errorKey,
                                Object[] errorParams) {
    super(type, defaultMessage, Status.NOT_FOUND, null, null, null,
            getAlertParameters(defaultMessage, errorCode, errorKey, errorParams));

    this.errorKey = errorKey;
    this.errorParams = errorParams;
  }

  /**
   * Tạo map các tham số lỗi
   *
   * @param message Thông điệp lỗi
   * @param errorCode Mã lỗi
   * @param errorKey Khóa lỗi
   * @param errorParams Các tham số bổ sung
   * @return Map các tham số lỗi
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