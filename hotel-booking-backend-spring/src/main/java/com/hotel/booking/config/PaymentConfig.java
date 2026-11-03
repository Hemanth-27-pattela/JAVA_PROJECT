package com.hotel.booking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.services.payment.PaymentGatewayService;
import com.hotel.booking.services.payment.SimulatedPaymentGatewayService;
import com.hotel.booking.services.payment.StripePaymentGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    private static final Logger log = LoggerFactory.getLogger(PaymentConfig.class);

    @Bean
    @ConditionalOnProperty(name = "app.payment.mode", havingValue = "stripe")
    public PaymentGatewayService stripePaymentGatewayService(
            @Value("${stripe.api-key:}") String stripeApiKey,
            @Value("${stripe.api-url:https://api.stripe.com}") String stripeApiUrl,
            ObjectMapper objectMapper
    ) {
        log.info("Configuring StripePaymentGatewayService for real Stripe payments (endpoint: {})", stripeApiUrl);
        return new StripePaymentGatewayService(stripeApiKey, stripeApiUrl, null, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "app.payment.mode", havingValue = "simulation", matchIfMissing = true)
    public PaymentGatewayService simulatedPaymentGatewayService() {
        log.info("Configuring SimulatedPaymentGatewayService (development/testing simulation mode)");
        return new SimulatedPaymentGatewayService();
    }
}

