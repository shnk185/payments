package org.ngs.payments.exception.external;

import org.ngs.payments.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ExternalApiServerException extends ApiException {
    public ExternalApiServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
