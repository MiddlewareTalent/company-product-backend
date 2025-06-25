package com.accesshr.emsbackend.EmployeeController.PaymentController;

import com.accesshr.emsbackend.EmployeeController.Config.TenantContext;
import com.accesshr.emsbackend.Entity.ClientDetails;
import com.accesshr.emsbackend.Service.ClientDetailsService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import jakarta.annotation.PostConstruct;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })

public class PaymentController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Autowired
    private ClientDetailsService clientDetailsService;

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

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, Object>> createCheckoutSession(@RequestBody ClientDetails clientDetails)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("plan", clientDetails.getPlan());
        metadata.put("schemaName", clientDetails.getSchemaName());
        metadata.put("price", String.valueOf(clientDetails.getPrice()));
        metadata.put("noOfEmployees", String.valueOf(clientDetails.getNoOfEmployees()));

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("gbp")
                                .setUnitAmount((long) clientDetails.getPrice() + 0L)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(clientDetails.getPlan() + " Plan")
                                                .build())
                                .build())
                .setQuantity(1L)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(lineItem)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .putAllMetadata(metadata)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .build();

        Session session = Session.create(params);

        Map<String, Object> response = new HashMap<>();
        response.put("url", session.getUrl());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        Stripe.apiKey = stripeApiKey;

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

                // if (session != null) {
                // PaymentRecord payment = new PaymentRecord();
                // payment.setSessionId(session.getId());
                // payment.setAmount(session.getAmountTotal());
                // payment.setStatus("paid");
                // payment.setPlan(session.getMetadata().get("plan"));
                // payment.setUserId(session.getMetadata().get("userId"));
                // payment.setCompany(session.getMetadata().get("company"));

                // paymentRepository.save(payment);
                // }
                String tenantId = session.getMetadata().get("schemaName");
                String country=null;
                if (tenantId != null) {
                    int index = tenantId.indexOf("_");
                    country = index != -1 ? tenantId.substring(0, index) : tenantId;
                }
                TenantContext.setCountry(country);
                TenantContext.setTenantId("public");
                if (session != null) {
                    ClientDetails clientDetails = clientDetailsService
                            .getClientDetailsBySchema(session.getMetadata().get("schemaName"));
                    clientDetails.setPrice(Double.parseDouble(session.getMetadata().get("price")));
                    clientDetails.setNoOfEmployees(Integer.parseInt(session.getMetadata().get("noOfEmployees")));
                    clientDetails.setPlan(session.getMetadata().get("plan"));
                    clientDetailsService.updateClientDetails(clientDetails.getId(), clientDetails);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook error");
        }

        return ResponseEntity.ok("Received");
    }

}
