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
 * Handler Forbidden Exception
 * Xử lý ngoại lệ 403 - Forbidden
 *
 * @author minhquang
 * @version 1.0
 * @since 2025-03-11
 */
@Getter
public class ForbiddenException extends AbstractThrowableProblem {

  private static final long serialVersionUID = -1234567890123456789L;

  private final String errorKey;
  private final Object[] errorParams;

  /**
   * Constructor với mã lỗi `MessageCode`
   *
   * @param errorCode Mã lỗi từ enum `MessageCode`
   */
  public ForbiddenException(MessageCode errorCode) {
    this(Labels.getLabels(errorCode.getKey()), errorCode.name(), errorCode.getKey());
  }

  /**
   * Constructor với thông điệp, mã lỗi và khóa lỗi
   *
   * @param defaultMessage Thông điệp lỗi mặc định
   * @param errorCode Mã lỗi
   * @param errorKey Khóa lỗi
   */
  public ForbiddenException(String defaultMessage, String errorCode, String errorKey) {
    this(ApiConstants.ErrorType.DEFAULT_TYPE, defaultMessage, errorCode, errorKey, new Object[0]);
  }

  /**
   * Constructor đầy đủ với tất cả tham số
   *
   * @param type Loại lỗi
   * @param defaultMessage Thông điệp lỗi
   * @param errorCode Mã lỗi
   * @param errorKey Khóa lỗi
   * @param errorParams Danh sách tham số lỗi
   */
  public ForbiddenException(URI type, String defaultMessage, String errorCode, String errorKey, Object[] errorParams) {
    super(type, defaultMessage, Status.FORBIDDEN, null, null, null,
            getAlertParameters(defaultMessage, errorCode, errorKey, errorParams));

    this.errorKey = errorKey;
    this.errorParams = errorParams;
  }

  /**
   * Tạo map chứa thông tin lỗi
   *
   * @param message Thông điệp lỗi
   * @param errorCode Mã lỗi
   * @param errorKey Khóa lỗi
   * @param errorParams Tham số lỗi
   * @return Map chứa thông tin lỗi
   */
  private static Map<String, Object> getAlertParameters(String message, String errorCode, String errorKey, Object[] errorParams) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(ApiConstants.ErrorKey.MESSAGE, message);
    parameters.put(ApiConstants.ErrorKey.ERROR_CODE, errorCode);
    parameters.put(ApiConstants.ErrorKey.ERROR_KEY, errorKey);
    parameters.put(ApiConstants.ErrorKey.PARAMS, Arrays.asList(errorParams));
    return parameters;
  }
}
