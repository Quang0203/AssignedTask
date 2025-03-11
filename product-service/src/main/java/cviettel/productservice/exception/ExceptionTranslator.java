package cviettel.productservice.exception;

import cviettel.orderservice.dto.response.common.ObjectResponse;
import cviettel.orderservice.exception.CustomKeycloakException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import java.time.Instant;

/**
 * Bộ xử lý ngoại lệ toàn cục cho các controller REST.
 * Lớp này xử lý các ngoại lệ cụ thể và trả về các HTTP response tương ứng.
 *
 * @author minhquang
 * @version 1.0
 * @since 2024-11-25
 */
@Slf4j
@RestControllerAdvice
public class ExceptionTranslator implements ProblemHandling {

    private static final String ERROR_CODE_KEY = "errorCode";

    /**
     * Xử lý BadRequestAlertException và
     * trả về một ObjectResponse với mã status HTTP 400 (Bad Request).
     *
     * @param ex Đối tượng BadRequestAlertException.
     * @return ObjectResponse chứa thông báo lỗi.
     */
    @ExceptionHandler(BadRequestAlertException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ObjectResponse<String, Instant> handleBadRequestAlertException(BadRequestAlertException ex) {
        return new ObjectResponse<>(ex.getParameters().get(ERROR_CODE_KEY).toString(), ex.getMessage(), Instant.now());
    }

    /**
     * Xử lý InternalServerErrorException và
     * trả về một ObjectResponse với mã status HTTP 500 (Internal Server Error).
     *
     * @param ex Đối tượng InternalServerErrorException.
     * @return ObjectResponse chứa thông báo lỗi.
     */
    @ExceptionHandler(InternalServerErrorException.class)
    public ObjectResponse<String, Instant> handleInternalServerErrorException(InternalServerErrorException ex) {
        return new ObjectResponse<>(ex.getParameters().get(ERROR_CODE_KEY).toString(), ex.getMessage(), Instant.now());
    }

    /**
     * Xử lý UnauthorizedException và
     * trả về một ObjectResponse với mã status HTTP 401 (Unauthorized).
     *
     * @param ex Đối tượng UnauthorizedException.
     * @return ObjectResponse chứa thông báo lỗi.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ObjectResponse<String, Instant> handleUnauthorizedException(UnauthorizedException ex) {
        return new ObjectResponse<>(ex.getParameters().get(ERROR_CODE_KEY).toString(), ex.getMessage(), Instant.now());
    }

    /**
     * Xử lý ForbiddenException và
     * trả về một ObjectResponse với mã status HTTP 403 (Forbidden).
     *
     * @param ex Đối tượng ForbiddenException.
     * @return ObjectResponse chứa thông báo lỗi.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ObjectResponse<String, Instant> handleForbiddenException(ForbiddenException ex) {
        return new ObjectResponse<>(ex.getParameters().get(ERROR_CODE_KEY).toString(), ex.getMessage(), Instant.now());
    }

    /**
     * Xử lý NotFoundAlertException và
     * trả về một ObjectResponse với mã status HTTP 404 (Not Found).
     *
     * @param ex Đối tượng NotFoundAlertException.
     * @return ObjectResponse chứa thông báo lỗi.
     */
    @ExceptionHandler(NotFoundAlertException.class)
    public ObjectResponse<String, Instant> handleNotFoundAlertException(NotFoundAlertException ex) {
        return new ObjectResponse<>(ex.getParameters().get(ERROR_CODE_KEY).toString(), ex.getMessage(), Instant.now());
    }

    /**
     * Xử lý CustomKeycloakException và
     * trả về một ObjectResponse với mã status HTTP 500 (Internal Server Error).
     *
     * @param ex Đối tượng CustomKeycloakException.
     * @return ObjectResponse chứa thông báo lỗi.
     */
    @ExceptionHandler(CustomKeycloakException.class)
    public ObjectResponse<String, Instant> handleCustomKeycloakException(CustomKeycloakException ex) {
        return new ObjectResponse<>(ex.getParameters().get(ERROR_CODE_KEY).toString(), ex.getMessage(), Instant.now());
    }

    /**
     * Xử lý OtherException và
     * trả về một ObjectResponse với mã status HTTP 500 (Internal Server Error).
     *
     * @param ex Đối tượng OtherException.
     * @return ObjectResponse chứa thông báo lỗi.
     */
    @ExceptionHandler(OtherException.class)
    public ObjectResponse<String, Instant> handleOtherException(OtherException ex) {
        return new ObjectResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage(), Instant.now());
    }
}

