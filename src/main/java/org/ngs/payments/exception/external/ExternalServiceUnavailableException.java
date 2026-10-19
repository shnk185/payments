package org.ngs.payments.exception.external;

import org.ngs.payments.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ExternalServiceUnavailableException extends ApiException {
    public ExternalServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
