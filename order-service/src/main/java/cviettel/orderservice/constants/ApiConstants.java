package cviettel.orderservice.constants;

import java.net.URI;

public interface ApiConstants {
    public interface ErrorCode {
        public static final int FAILURE = 2;

        public static final int SUCCESS = 1;
    }

    public interface ErrorKey {
        public static final String ERROR_KEY = "errorKey";

        public static final String ERROR_CODE = "errorCode";

        public static final String FIELD_ERRORS = "fieldErrors";

        public static final String MESSAGE = "message";

        public static final String PARAMS = "params";

        public static final String PATH = "path";

        public static final String VIOLATIONS = "violations";
    }

    public interface ErrorType {
        public static final String _PROBLEM_BASE_URL = "http://www.its.cms.vn/problem";

        public static final URI APPOINTMENT_TIME_AVAILABLE_TYPE = URI
                .create(_PROBLEM_BASE_URL + "/appointment-time-used");

        public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(_PROBLEM_BASE_URL + "/constraint-violation");

        public static final URI DEFAULT_TYPE = URI.create(_PROBLEM_BASE_URL + "/problem-with-message");

        public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(_PROBLEM_BASE_URL + "/email-already-used");

        public static final URI INVALID_PASSWORD_TYPE = URI.create(_PROBLEM_BASE_URL + "/invalid-password");

        public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(_PROBLEM_BASE_URL + "/login-already-used");

        public static final URI PHONE_ALREADY_USED_TYPE = URI.create(_PROBLEM_BASE_URL + "/phone-already-used");
    }

    public interface HttpHeaders {
        public static final String LINK_FORMAT = "<{0}>; rel=\"{1}\"";

        public static final String X_ACTION_MESSAGE = "X-Action-Message";

        public static final String X_ACTION_MESSAGE_KEY = "X-Action-Message-Key";

        public static final String X_ACTION_PARAMS = "X-Action-Params";

        public static final String X_FORWARDED_FOR = "X-Forwarded-For";

        public static final String X_SIGNATURE = "X-SIGNATURE";

        public static final String X_TOTAL_COUNT = "X-Total-Count";

        public static final String X_TRANSACTION_ID = "X-TRANSACTION-ID";

        public static final String CLIENT_MESSAGE_ID = "clientMessageId";

        public static final String TRANSACTION_ID = "transactionId";
    }

    public interface Pagination {
        public static final String FIRST = "first";

        public static final String LAST = "last";

        public static final String NEXT = "next";

        public static final String PAGE = "page";

        public static final String PREV = "prev";

        public static final String SIZE = "size";
    }

    public interface Header {
        public static final String AUTHORIZATION_HEADER = "Authorization";

        public static final String BEARER_START = "Bearer ";

        public static final String BASIC_START = "Basic ";

        public static final String PRIVILEGES = "privileges";

        public static final String HASHKEY = "hash-key";

        public static final String LOCALE = "locale";

        public static final String REFRESH_TOKEN = "refresh-token";

        public static final String USER = "user";
    }
}
