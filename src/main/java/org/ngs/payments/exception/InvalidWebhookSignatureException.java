package org.ngs.payments.exception;

import org.springframework.http.HttpStatus;

public class InvalidWebhookSignatureException extends ApiException {
    public InvalidWebhookSignatureException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
