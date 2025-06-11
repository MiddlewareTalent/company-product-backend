package com.accesshr.emsbackend.EmployeeController.PaymentController;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})

public class PaymentController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @PostMapping("/create-checkout-session/{price}")
    public ResponseEntity<Map<String, Object>> createCheckoutSession(@PathVariable int price) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        lineItems.add(
                SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("gbp")
                                        .setUnitAmount(price + 0L)
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName("Sample Product")
                                                        .build())
                                        .build())
                        .setQuantity(1L)
                        .build());

        SessionCreateParams params = SessionCreateParams.builder()
                .addAllLineItem(lineItems)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .build();

        Session session = Session.create(params);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", session.getId());
        responseData.put("url", session.getUrl()); // ✅ Add this line

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestParam String session_id) throws StripeException {
        Session session = Session.retrieve(session_id);

        Map<String, Object> response = new HashMap<>();
        response.put("session_id", session_id);
        response.put("session_status", session.getPaymentStatus());

        // Fallback check via PaymentIntent
        if (session.getPaymentIntent() != null) {
            PaymentIntent intent = PaymentIntent.retrieve(session.getPaymentIntent());

            response.put("intent_id", intent.getId());
            response.put("intent_status", intent.getStatus()); // should be 'succeeded'
            response.put("paid", "succeeded".equals(intent.getStatus()));
        } else {
            response.put("paid",
                    "complete".equals(session.getPaymentStatus()) || "paid".equals(session.getPaymentStatus()));
        }

        return ResponseEntity.ok(response);
    }

}
