package com.accesshr.emsbackend.EmployeeController.PaymentController;

import com.accesshr.emsbackend.EmployeeController.Config.TenantContext;
import com.accesshr.emsbackend.Entity.ClientDetails;
import com.accesshr.emsbackend.Service.ClientDetailsService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


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

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    // SIMPLE SESSION CREATION FOR TESTING
    @PostMapping("/create-checkout-session/{price}")
    public ResponseEntity<Map<String, Object>> createSimpleCheckoutSession(@PathVariable int price) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        lineItems.add(SessionCreateParams.LineItem.builder()
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("gbp")
                        .setUnitAmount((long) price)
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
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

        Map<String, Object> response = new HashMap<>();
        response.put("id", session.getId());
        response.put("url", session.getUrl());
        return ResponseEntity.ok(response);
    }

    // SESSION CREATION WITH CLIENT METADATA
    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, Object>> createCheckoutSessionWithMetadata(@RequestBody ClientDetails clientDetails) throws StripeException {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("plan", clientDetails.getPlan());
        metadata.put("schemaName", clientDetails.getSchemaName());
        metadata.put("price", String.valueOf(clientDetails.getPrice()));
        metadata.put("noOfEmployees", String.valueOf(clientDetails.getNoOfEmployees()));

        System.out.println("Creating checkout session with metadata: " + metadata);

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("gbp")
                        .setUnitAmount((long) clientDetails.getPrice())
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
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
        response.put("id", session.getId());

        System.out.println("Created session: " + session.getId());
        System.out.println("Session metadata: " + session.getMetadata());

        return ResponseEntity.ok(response);
    }

    // VERIFY PAYMENT STATUS (OPTIONAL)
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestParam String session_id) throws StripeException {
        Session session = Session.retrieve(session_id);
        Map<String, Object> response = new HashMap<>();
        response.put("session_id", session_id);
        response.put("session_status", session.getPaymentStatus());

        if (session.getPaymentIntent() != null) {
            PaymentIntent intent = PaymentIntent.retrieve(session.getPaymentIntent());
            response.put("intent_id", intent.getId());
            response.put("intent_status", intent.getStatus());
            response.put("paid", "succeeded".equals(intent.getStatus()));
        } else {
            response.put("paid", "complete".equals(session.getPaymentStatus()));
        }

        return ResponseEntity.ok(response);
    }

    // STRIPE WEBHOOK ENDPOINT
    
@PostMapping("/webhook")
public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
    try {
        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

        if (session == null) {
            // Fallback: manually extract session ID from raw JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            String sessionId = rootNode.path("data").path("object").path("id").asText();
            session = Session.retrieve(sessionId);
        }

        System.out.println("Webhook received for session: " + session.getId());

        if (session != null && session.getMetadata() != null) {
            String schemaName = session.getMetadata().get("schemaName");
            String tenantId=schemaName;
            System.out.print("schema name"+schemaName);
            String country=null;

        if (tenantId!=null) {
            int index = tenantId.indexOf("_");
            country = index != -1 ? tenantId.substring(0, index) : tenantId;
        }
        TenantContext.setCountry(country);
        TenantContext.setTenantId("public");
        System.out.println("country id"+country);

            if (schemaName != null) {
                ClientDetails clientDetails = clientDetailsService.getClientDetailsBySchema(schemaName);
                clientDetails.setPrice(Double.parseDouble(session.getMetadata().get("price")));
                clientDetails.setNoOfEmployees(Integer.parseInt(session.getMetadata().get("noOfEmployees")));
                clientDetails.setPlan(session.getMetadata().get("plan"));
                clientDetailsService.updateClientDetails(clientDetails.getId(), clientDetails);

                System.out.println("Client details updated.");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
    }

    return ResponseEntity.ok("Webhook received");
}

}
