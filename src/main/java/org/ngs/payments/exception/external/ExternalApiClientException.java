package org.ngs.payments.exception.external;

import org.ngs.payments.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ExternalApiClientException extends ApiException {
    public ExternalApiClientException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
