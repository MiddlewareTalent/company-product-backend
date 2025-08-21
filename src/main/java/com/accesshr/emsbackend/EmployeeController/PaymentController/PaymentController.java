package com.accesshr.emsbackend.EmployeeController.PaymentController;

import com.accesshr.emsbackend.Dto.EmployeeManagerDTO;
import com.accesshr.emsbackend.EmployeeController.Config.TenantContext;
import com.accesshr.emsbackend.Entity.ClientDetails;
import com.accesshr.emsbackend.Entity.CountryServerConfig;
import com.accesshr.emsbackend.Entity.EmployeeManager;
import com.accesshr.emsbackend.Service.ClientDetailsService;
import com.accesshr.emsbackend.Service.EmployeeManagerService;
import com.accesshr.emsbackend.Service.TenantSchemaService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.Price;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.param.CustomerListParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionCancelParams;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173","https://talents-flow-live-server.azurewebsites.net/"})
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

    @Autowired
    private TenantSchemaService tenantSchemaService;

    @Autowired
    private EmployeeManagerService employeeManagerService;

    @Value("${stripe.webhook.secrets}")
    private String endpointSecret;


    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @PostMapping("/create-checkout-session/{company}")
    public ResponseEntity<Map<String, Object>> createSubscriptionCheckoutSession(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("country") String country,
            @RequestParam("noOfEmployees") int noOfEmployees,
            @RequestParam("plan") String plan,
            @RequestParam("task") boolean task,
            @RequestParam("organizationChart") boolean organizationChart,
            @RequestParam("leaveManagement") boolean leaveManagement,
            @RequestParam("timeSheet") boolean timeSheet,
            @RequestParam("basePriceId") String basePriceId,
            @RequestParam(value = "taskAddonPriceId", required = false) String taskAddonPriceId,
            @RequestParam(value = "timeSheetAddonPriceId", required = false) String timeSheetAddonPriceId,
            @PathVariable String company
    ) {
        try {
            if (company == null || company.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Company name is required"));
            }

            String schemaName = country.toLowerCase() + "_" + company.trim().replaceAll("\\s+", "_").toLowerCase();

            // === Create Line Items ===
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

            // Base plan
            if (basePriceId == null || basePriceId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Base price ID is required"));
            }

            lineItems.add(SessionCreateParams.LineItem.builder()
                    .setPrice(basePriceId)
                    .setQuantity(1L)
                    .build());

            // Task add-on
            if (task && taskAddonPriceId != null && !taskAddonPriceId.isBlank()) {
                lineItems.add(SessionCreateParams.LineItem.builder()
                        .setPrice(taskAddonPriceId)
                        .setQuantity(1L)
                        .build());
            }

            // Timesheet add-on
            if (timeSheet && timeSheetAddonPriceId != null && !timeSheetAddonPriceId.isBlank()) {
                lineItems.add(SessionCreateParams.LineItem.builder()
                        .setPrice(timeSheetAddonPriceId)
                        .setQuantity(1L)
                        .build());
            }

            // === Metadata ===
            Map<String, String> metadata = new HashMap<>();
            metadata.put("firstName", firstName);
            metadata.put("lastName", lastName);
            metadata.put("email", email);
            metadata.put("password", password);
            metadata.put("country", country);
            metadata.put("noOfEmployees", String.valueOf(noOfEmployees));
            metadata.put("plan", plan);
            metadata.put("task", String.valueOf(task));
            metadata.put("organizationChart", String.valueOf(organizationChart));
            metadata.put("leaveManagement", String.valueOf(leaveManagement));
            metadata.put("timeSheet", String.valueOf(timeSheet));
            metadata.put("companyName", company);
            metadata.put("schemaName", schemaName);


            // === Stripe Session ===
            SessionCreateParams.Builder sessionParamsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl + schemaName + "/login?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:3000/cancel")
                    .setCustomerEmail(email)
                    .putAllMetadata(metadata)
                    .setAllowPromotionCodes(true)
                    .setSubscriptionData(
                            SessionCreateParams.SubscriptionData.builder()
                                    .setTrialPeriodDays(7L)
                                    .build()
                    );

            lineItems.forEach(sessionParamsBuilder::addLineItem);

            Session session = Session.create(sessionParamsBuilder.build());

            return ResponseEntity.ok(Map.of(
                    "id", session.getId(),
                    "url", session.getUrl()
            ));

        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Stripe error: " + e.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + ex.getMessage()));
        }
    }



    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeEventCheckout(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("✅ Webhook triggered");
        System.out.println("➡️ Signature: " + sigHeader);

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            System.out.println("✅ Stripe event received: " + event.getType());

            if ("checkout.session.completed".equals(event.getType())) {

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(payload);
                String sessionId = rootNode.path("data").path("object").path("id").asText();

                Session session = Session.retrieve(sessionId);
                String email = session.getCustomerEmail();
                Map<String, String> metadata = session.getMetadata();

                // Register Admin
                String firstName = metadata.get("firstName");
                String lastName = metadata.get("lastName");
                String password = metadata.get("password");
                String country = metadata.get("country");
                String company = metadata.get("companyName");
                String plan = metadata.get("plan");
                int noOfEmployees = Integer.parseInt(metadata.get("noOfEmployees"));
                boolean task = Boolean.parseBoolean(metadata.getOrDefault("task", "false"));
                boolean organizationChart = Boolean.parseBoolean(metadata.getOrDefault("organizationChart", "false"));
                boolean leaveManagement = Boolean.parseBoolean(metadata.getOrDefault("leaveManagement", "false"));
                boolean timeSheet = Boolean.parseBoolean(metadata.getOrDefault("timeSheet", "false"));

                String subscriptionId = session.getSubscription();
                double price = 0.0;
                String invoiceUrl = null;

                if (subscriptionId != null) {
                    Subscription subscription = Subscription.retrieve(subscriptionId);
                    if (!subscription.getItems().getData().isEmpty()) {
                        SubscriptionItem item = subscription.getItems().getData().get(0);
                        Price stripePrice = Price.retrieve(item.getPrice().getId());
                        price = stripePrice.getUnitAmount() / 100.0;
                    }

                    // ✅ Get latest invoice for the subscription
                    String invoiceId = subscription.getLatestInvoice();
                    if (invoiceId != null) {
                        Invoice invoice = Invoice.retrieve(invoiceId);
                        invoiceUrl = invoice.getHostedInvoiceUrl();
                    }
                }

                System.out.println("💰 Retrieved price: " + price);
                System.out.println("📨 Invoice link: " + invoiceUrl);

                registerAdmin(firstName, lastName, email, country, noOfEmployees, plan, price, company, password, task, organizationChart, leaveManagement, timeSheet);

                // ✅ Send invoice email
                sendInvoiceEmail(email, firstName + " " + lastName, company, plan, price, invoiceUrl);

                return ResponseEntity.ok("✅ Webhook handled: checkout.session.completed");
            }

            return ResponseEntity.ok("ℹ️ Unhandled event type: " + event.getType());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook error: " + e.getMessage());
        }
    }


    @Autowired
    private JavaMailSender mailSender;

    public void sendInvoiceEmail(String toEmail, String fullName, String company, String plan, double price, String invoiceUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("✅ Subscription Invoice - " + company);
            message.setText("Hello " + fullName + ",\n\n" +
                    "Thank you for subscribing to the " + plan + " plan.\n" +
                    "Amount Paid: £" + price + "\n" +
                    "Company: " + company + "\n\n" +
                    "You can view or download your invoice here:\n" + invoiceUrl + "\n\n" +
                    "Best regards,\nAccessHR Team");

            mailSender.send(message);

            System.out.println("✅ Invoice email sent to: " + toEmail);
        } catch (Exception e) {
            System.out.println("❌ Failed to send invoice email: " + e.getMessage());
        }
    }







    public String registerAdmin(String firstName, String lastName, String email,
                                String country, int noOfEmployees, String plan, double price,
                                String company, String password , boolean task, boolean organizationChart,
                                boolean leaveManagement, boolean timeSheet) throws Exception {
        String schemaName = country + "_" + company.trim().replace(" ", "_");

        // Validate country
        CountryServerConfig serverConfig = CountryServerConfig.valueOf(country.trim().toUpperCase());

        try {
            // Set tenant context
            TenantContext.setTenantId(schemaName);
            TenantContext.setCountry(country);

            // 1. Create schema and tables
            tenantSchemaService.createTenant(schemaName, country);

            // 2. Save admin in tenant schema
            EmployeeManagerDTO employeeManagerDTO = new EmployeeManagerDTO();
            employeeManagerDTO.setFirstName(firstName);
            employeeManagerDTO.setLastName(lastName);
            employeeManagerDTO.setEmail(email);
            employeeManagerDTO.setCorporateEmail(email);
            employeeManagerDTO.setRole("admin");
            employeeManagerDTO.setPassword(password);
            employeeManagerDTO.setLeaveManagement(leaveManagement);
            employeeManagerDTO.setTask(task);
            employeeManagerDTO.setTimeSheet(timeSheet);
            employeeManagerDTO.setOrganizationChart(organizationChart);

            employeeManagerService.addAdmin(schemaName, employeeManagerDTO);

            // 3. Switch to public schema
            TenantContext.setTenantId("public");
            TenantContext.setCountry(country);

            ClientDetails clientDetails = new ClientDetails();
            clientDetails.setFirstName(firstName);
            clientDetails.setLastName(lastName);
            clientDetails.setCompanyName(company);
            clientDetails.setEmail(email);
            clientDetails.setCountry(country);
            clientDetails.setSchemaName(schemaName);
            clientDetails.setServerUrl(serverConfig.getServerUrl());
            clientDetails.setNoOfEmployees(noOfEmployees);
            clientDetails.setStarDate(LocalDate.now());
            clientDetails.setEndDate(LocalDate.now().plusDays(365));
            clientDetails.setPlan(plan);
            clientDetails.setTask(task);
            clientDetails.setOrganizationChart(organizationChart);
            clientDetails.setLeaveManagement(leaveManagement);
            clientDetails.setTimeSheet(timeSheet);
            clientDetails.setPrice(price);

            clientDetailsService.createClient(clientDetails);

            return schemaName;
        } finally {
            TenantContext.clear();
        }
    }



        @PostMapping("/upgrade")
        public ResponseEntity<?> upgradeMultipleSubscriptions(
                @RequestParam("email") String email,
                @RequestParam("firstName") String firstName,
                @RequestParam("lastName") String lastName,
                @RequestParam("country") String country,
                @RequestParam("noOfEmployees") int noOfEmployees,
                @RequestParam("plan") String plan,
                @RequestParam("organizationChart") boolean organizationChart,
                @RequestParam("leaveManagement") boolean leaveManagement,

                @RequestParam("baseSubscriptionId") String baseSubscriptionId,
                @RequestParam("basePriceId") String basePriceId,

                @RequestParam(value = "task", required = false) Boolean task,
                @RequestParam(value = "taskAddonPriceId", required = false) String taskAddonPriceId,
                @RequestParam(value = "taskSubscriptionId", required = false) String taskSubscriptionId,

                @RequestParam(value = "timeSheet", required = false) Boolean timeSheet,
                @RequestParam(value = "timeSheetAddonPriceId", required = false) String timeSheetAddonPriceId,
                @RequestParam(value = "timesheetSubscriptionId", required = false) String timesheetSubscriptionId
        ) {
            try {
                List<Map<String, Object>> sessions = new ArrayList<>();

                // === Common metadata ===
                Map<String, String> metadata = new HashMap<>();
                metadata.put("email", email);
                metadata.put("firstName", firstName);
                metadata.put("lastName", lastName);
                metadata.put("country", country);
                metadata.put("noOfEmployees", String.valueOf(noOfEmployees));
                metadata.put("plan", plan);
                metadata.put("organizationChart", String.valueOf(organizationChart));
                metadata.put("leaveManagement", String.valueOf(leaveManagement));

                // === Base Plan Upgrade ===
                if (baseSubscriptionId != null && basePriceId != null) {
                    Session baseSession = createUpgradeSession(email, baseSubscriptionId, basePriceId, metadata);
                    sessions.add(Map.of("type", "base", "id", baseSession.getId(), "url", baseSession.getUrl()));
                }

                // === Task Add-on Upgrade ===
                if (Boolean.TRUE.equals(task) && taskAddonPriceId != null && taskSubscriptionId != null) {
                    metadata.put("task", "true");
                    Session taskSession = createUpgradeSession(email, taskSubscriptionId, taskAddonPriceId, metadata);
                    sessions.add(Map.of("type", "task", "id", taskSession.getId(), "url", taskSession.getUrl()));
                }

                // === Timesheet Add-on Upgrade ===
                if (Boolean.TRUE.equals(timeSheet) && timeSheetAddonPriceId != null && timesheetSubscriptionId != null) {
                    metadata.put("timeSheet", "true");
                    Session timesheetSession = createUpgradeSession(email, timesheetSubscriptionId, timeSheetAddonPriceId, metadata);
                    sessions.add(Map.of("type", "timesheet", "id", timesheetSession.getId(), "url", timesheetSession.getUrl()));
                }

                return ResponseEntity.ok(Map.of("sessions", sessions));

            } catch (StripeException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Stripe error: " + e.getMessage()));
            } catch (Exception ex) {
                ex.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Internal server error: " + ex.getMessage()));
            }
        }

        // === Helper Method to Create Upgrade Session ===
        private Session createUpgradeSession(String email, String subscriptionId, String priceId, Map<String, String> metadata) throws StripeException {
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPrice(priceId)
                    .setQuantity(1L)
                    .build();

            SessionCreateParams.SubscriptionData subscriptionData = SessionCreateParams.SubscriptionData.builder()
                    .putExtraParam("subscription", subscriptionId)
                    .build();

            SessionCreateParams sessionParams = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:3000/cancel")
                    .setCustomerEmail(email)
                    .putAllMetadata(metadata)
                    .setAllowPromotionCodes(true)
                    .setSubscriptionData(subscriptionData)
                    .addLineItem(lineItem)
                    .build();

            return Session.create(sessionParams);
        }



    @PutMapping("/update-subscription")
    public ResponseEntity<?> updateSubscriptionPlan(
            @RequestParam("subscriptionId") String subscriptionId,
            @RequestParam("newPlan") String newPlan
    ) {
        try {
            // Step 1: Map plan to new Stripe Price ID
            Map<String, String> priceIdMap = Map.of(
                    "Starter", "price_1RfeGVQv4NN0qcyWVN3nV4Q4",
                    "Premium", "price_1RfeGwQv4NN0qcyW8379T8ZS",
                    "PremiumPlus", "price_1RfeHAQv4NN0qcyWtpCk7ceK"
            );

            String newPriceId = priceIdMap.get(newPlan);
            if (newPriceId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid new plan selected"));
            }

            // Step 2: Retrieve existing subscription
            Subscription subscription = Subscription.retrieve(subscriptionId);

            // Step 3: Get existing subscription item ID
            String subscriptionItemId = subscription.getItems().getData().get(0).getId();

            // Step 4: Update subscription with new price
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .addItem(
                            SubscriptionUpdateParams.Item.builder()
                                    .setId(subscriptionItemId)
                                    .setPrice(newPriceId)
                                    .build()
                    )
                    .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.CREATE_PRORATIONS) // Optional: bill difference
                    .build();

            Subscription updatedSubscription = subscription.update(params);

            return ResponseEntity.ok(Map.of(
                    "message", "Subscription updated successfully",
                    "subscriptionId", updatedSubscription.getId(),
                    "newPlan", newPlan
            ));

        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Stripe error: " + e.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + ex.getMessage()));
        }
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
                    double paidAmount=Double.parseDouble(session.getMetadata().get("price"))/100;
                    clientDetails.setPrice(clientDetails.getPrice()+paidAmount);
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

    @PostMapping("/cancel-subscription")
    public ResponseEntity<String> cancelSubscriptionAtPeriodEnd(@RequestParam String email) {
        try {
            // Step 1: Find customer by email
            CustomerListParams customerListParams = CustomerListParams.builder()
                    .setEmail(email)
                    .setLimit(1L) // only need one
                    .build();

            CustomerCollection customers = Customer.list(customerListParams);

            if (customers.getData().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ No customer found with email: " + email);
            }

            Customer customer = customers.getData().get(0);

            // Step 2: List all subscriptions (not just ACTIVE)
            SubscriptionListParams subscriptionListParams = SubscriptionListParams.builder()
                    .setCustomer(customer.getId())
                    .build(); // fetch all statuses: trialing, active, etc.

            SubscriptionCollection subscriptions = Subscription.list(subscriptionListParams);

            if (subscriptions.getData().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ No subscription found for this customer.");
            }

            Subscription subscription = subscriptions.getData().get(0);
            String status = subscription.getStatus(); // "trialing", "active", etc.

            // Step 3: Only allow cancelation if trialing or active
            if ("trialing".equals(status) || "active".equals(status)) {
                Map<String, Object> updateParams = new HashMap<>();
                updateParams.put("cancel_at_period_end", true);

                Subscription updated = subscription.update(updateParams);

                long cancelAt = updated.getCancelAt();
                String cancelDate = Instant.ofEpochSecond(cancelAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toString();

                return ResponseEntity.ok("✅ Subscription (status: " + status + ") will be cancelled on: " + cancelDate);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("❌ Subscription is not cancellable (status: " + status + ").");
            }

        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Stripe error: " + e.getMessage());
        }
    }


}
