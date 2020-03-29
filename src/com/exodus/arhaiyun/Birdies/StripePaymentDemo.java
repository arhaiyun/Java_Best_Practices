package com.exodus.arhaiyun.Birdies;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;

import java.util.HashMap;
import java.util.Map;

public class StripePaymentDemo {
    public static void main(String[] args) {
        Stripe.apiKey = "sk_test_...";

        RequestOptions requestOptions = new RequestOptionsBuilder()
                .setApiKey("sk_test_...")
                .setIdempotencyKey("a1b2c3...")
                .setStripeAccount("acct_...")
                .build();
        Customer.list(null, requestOptions);
        Customer.retrieve("cus_123456789", requestOptions);

        Stripe.setMaxNetworkRetries(2);
        RequestOptions options = RequestOptions.builder()
                .setMaxNetworkRetries(2)
                .build();
        Customer.create(params, options);

        Stripe.setConnectTimeout(30 * 1000); // in milliseconds
        Stripe.setReadTimeout(80 * 1000);
        RequestOptions options = RequestOptions.builder()
                .setConnectTimeout(30 * 1000) // in milliseconds
                .setReadTimeout(80 * 1000)
                .build();
        Customer.create(params, options);


        Map<String, Object> customerMap = new HashMap<String, Object>();
        customerMap.put("description", "Example descriptipn");
        customerMap.put("email", "test@example.com");
        customerMap.put("payment_method", "pm_card_visa"); // obtained via Stripe.js

        Stripe.setAppInfo("MyAwesomePlugin", "1.2.34", "https://myawesomeplugin.info");

        try {
            Customer customer = Customer.create(customerMap);
            System.out.println(customer);
        } catch (StripeException e) {
            e.printStackTrace();
        }
    }
}

