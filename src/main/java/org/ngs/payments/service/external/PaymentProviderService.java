package org.ngs.payments.service.external;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.ngs.payments.config.properties.ApiConfig;
import org.ngs.payments.dto.external.request.PaymentSessionCreateRequest;
import org.ngs.payments.dto.external.response.PaymentSessionCreateResponse;
import org.ngs.payments.enums.Currency;
import org.ngs.payments.enums.ExternalApi;
import org.ngs.payments.exception.external.ExternalApiClientException;
import org.ngs.payments.exception.external.ExternalApiServerException;
import org.ngs.payments.exception.external.ExternalServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class PaymentProviderService {

    private final RestClient createCheckoutSessionRestClient;
    private final ApiConfig.ServiceConfig serviceConfig;

    public PaymentProviderService(@Qualifier("createCheckoutSessionRestClient") RestClient createCheckoutSessionRestClient,
                                  ApiConfig apiConfig) {
        this.createCheckoutSessionRestClient = createCheckoutSessionRestClient;
        this.serviceConfig = apiConfig.getServices().get(ExternalApi.PAYMENT_SERVICE_PROVIDER);
    }

    @Retry(name = "createCheckoutSessionProvider", fallbackMethod = "createPaymentSessionFallback")
    @CircuitBreaker(name = "createCheckoutSessionProvider", fallbackMethod = "createPaymentSessionCbFallback")
    public PaymentSessionCreateResponse createPaymentSession(BigDecimal amount, Currency currency, String successUrl,
                                                             String cancelUrl, Map<String, String> metadata,
                                                             String paymentUniqueId) {
        PaymentSessionCreateRequest paymentSessionCreateRequest = PaymentSessionCreateRequest.builder().amount(amount)
                .currency(currency).successUrl(successUrl).cancelUrl(cancelUrl).metadata(metadata).build();
        log.info("making create checkout session request {}", paymentSessionCreateRequest);
        return createCheckoutSessionRestClient.post()
                .uri(serviceConfig.getClients().get("CREATE_CHECKOUT_SESSION").getApiPath())
                .header("Idempotency-Key", paymentUniqueId)
                .body(paymentSessionCreateRequest).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ExternalApiClientException("create checkout session client exception " + res.getStatusCode());})
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ExternalApiServerException("create checkout session server exception " + res.getStatusCode());})
                .toEntity(PaymentSessionCreateResponse.class)
                .getBody();
    }

    public PaymentSessionCreateResponse createPaymentSessionCbFallback(
            BigDecimal amount, Currency currency, String successUrl,
            String cancelUrl, Map<String, String> metadata,
            String paymentUniqueId, CallNotPermittedException exception) {
        log.warn("Circuit breaker open for payment provider, paymentUniqueId={}", paymentUniqueId);
        throw new ExternalServiceUnavailableException("Payment provider is currently unavailable (circuit open), please retry later");
    }

    public PaymentSessionCreateResponse createPaymentSessionFallback(BigDecimal amount, Currency currency, String successUrl,
                                                                     String cancelUrl, Map<String, String> metadata,
                                                                     String paymentUniqueId, ExternalApiClientException exception) {
        throw exception;
    }

    public PaymentSessionCreateResponse createPaymentSessionFallback(BigDecimal amount, Currency currency, String successUrl,
                                                                     String cancelUrl, Map<String, String> metadata,
                                                                     String paymentUniqueId, Exception exception) {
        log.error("Payment session creation failed after retries, paymentUniqueId={}",
                paymentUniqueId, exception);
        throw new ExternalServiceUnavailableException("Payment provider is unavailable, please retry later");
    }
}
