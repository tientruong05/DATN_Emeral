package com.poly.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.util.List;

@Service
public class PayOSService {
    private final PayOS payOS;

    public PayOSService(
            @Value("${payos.clientId}") String clientId,
            @Value("${payos.apiKey}") String apiKey,
            @Value("${payos.checksumKey}") String checksumKey
    ) {
        this.payOS = new PayOS(clientId, apiKey, checksumKey);
    }

    public CheckoutResponseData createPaymentLink(long orderCode, int amount, String description, List<ItemData> items, String returnUrl, String cancelUrl) {
        try {
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .items(items)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();
            return payOS.createPaymentLink(paymentData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public WebhookData verifyPaymentWebhookData(Webhook webhookBody) {
        try {
            return payOS.verifyPaymentWebhookData(webhookBody);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 