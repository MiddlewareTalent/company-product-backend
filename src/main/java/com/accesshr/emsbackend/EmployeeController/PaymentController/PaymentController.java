package com.accesshr.emsbackend.EmployeeController.PaymentController;

import com.accesshr.emsbackend.Dto.EmployeeManagerDTO;
import com.accesshr.emsbackend.EmployeeController.Config.TenantContext;
import com.accesshr.emsbackend.Entity.ClientDetails;
import com.accesshr.emsbackend.Entity.CountryServerConfig;
import com.accesshr.emsbackend.Service.ClientDetailsService;
import com.accesshr.emsbackend.Service.EmployeeManagerService;
import com.accesshr.emsbackend.Service.TenantSchemaService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PaymentController {

    private final static Logger logger= LoggerFactory.getLogger(PaymentController.class);

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

    @Autowired
    private JavaMailSender mailSender;


    @Value("${stripe.webhook.secrets}")
    private String endpointSecret;


    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }



    @PostMapping("/create-checkout-session/{company}")
    public ResponseEntity<Map<String, Object>> createSimpleCheckoutSession(
            @Valid @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("country") String country,
            @RequestParam("noOfEmployees") int noOfEmployees,
            @RequestParam("plan") String plan,
            @RequestParam("price") double price,
            @RequestParam("task") boolean task,
            @RequestParam("organizationChart") boolean organizationChart,
            @RequestParam("leaveManagement") boolean leaveManagement,
            @RequestParam("timeSheet") boolean timeSheet,
            @PathVariable String company
    ) throws StripeException {

        // Generate schema name
        String schemaName = country + "_" + company.trim().replace(" ", "_");

        // ✅ Create or get existing Stripe customer
        Customer customer = getOrCreateCustomer(email, firstName, lastName);
        logger.info("email {}", email);

        // Line item
        List<SessionCreateParams.LineItem> lineItems = List.of(
                SessionCreateParams.LineItem.builder()
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("gbp")
                                .setUnitAmount((long) (price * 100))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(plan + " Plan - Talentflow")
                                        .build())
                                .build())
                        .setQuantity(1L)
                        .build()
        );

        // Metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("firstName", firstName);
        metadata.put("lastName", lastName);
        metadata.put("email", email);
        metadata.put("password", password);
        metadata.put("country", country);
        metadata.put("noOfEmployees", String.valueOf(noOfEmployees));
        metadata.put("plan", plan);
        metadata.put("price", String.valueOf(price));
        metadata.put("task", String.valueOf(task));
        metadata.put("organizationChart", String.valueOf(organizationChart));
        metadata.put("leaveManagement", String.valueOf(leaveManagement));
        metadata.put("timeSheet", String.valueOf(timeSheet));
        metadata.put("companyName", company);

//     System.out.println("📧 Email in DTO: " + employeeManagerDTO.getEmail());
// System.out.println("📧 Corp Email in DTO: " + employeeManagerDTO.getCorporateEmail());


        // Session create
        SessionCreateParams params = SessionCreateParams.builder()
                .addAllLineItem(lineItems)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + country.trim().toLowerCase() + "_" +
                        company.trim().replaceAll("\\s+", "_").toLowerCase() + "/login?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .setCustomer(customer.getId()) // ✅ Use customer ID, not email
                .putAllMetadata(metadata)
                .setAllowPromotionCodes(true)
                .setInvoiceCreation(
                        SessionCreateParams.InvoiceCreation.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return ResponseEntity.ok(Map.of("id", session.getId(), "url", session.getUrl()));
    }


    private Customer getOrCreateCustomer(String email, String firstName, String lastName) throws StripeException {
        // Search for existing customer
        CustomerListParams listParams = CustomerListParams.builder()
                .setEmail(email)
                .setLimit(1L)
                .build();

        CustomerCollection customers = Customer.list(listParams);
        if (!customers.getData().isEmpty()) {
            return customers.getData().get(0);
        }

        // If not found, create new customer
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .setName(firstName + " " + lastName)
                .setDescription("Talent Flow")
                .build();

        return Customer.create(params);
    }



    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeEventCheckout(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        logger.info("Webhook triggered");
        logger.info("Signature {}", sigHeader);
        logger.info("Secret {}",endpointSecret);

        try {
            // Validate the webhook signature
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            logger.info("Stripe event received {}", event.getType());

            if ("checkout.session.completed".equals(event.getType())) {
                logger.info("Check out session if condition entered");
                // ✅ Use Jackson to parse session ID
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(payload);
                String sessionId = rootNode.path("data").path("object").path("id").asText();
                logger.info("Extracted Session ID {}", sessionId);

                // ✅ Retrieve full session from Stripe
                Session fullSession = Session.retrieve(sessionId);
                logger.info("Full session retrieved");

                // String email = fullSession.getCustomerEmail();
                Map<String, String> metadata = fullSession.getMetadata();

                // System.out.println("✅ Email: " + email);
                // System.out.println("✅ Metadata: " + metadata);

                // Extract fields from metadata
                String company = metadata.get("companyName");
                String firstName = metadata.get("firstName");
                String lastName = metadata.get("lastName");
                String email = metadata.get("email");

                String country = metadata.get("country");
                String plan = metadata.get("plan");
                String password = metadata.get("password");
                int noOfEmployees = Integer.parseInt(metadata.get("noOfEmployees"));
                double price = Double.parseDouble(metadata.get("price"));
                boolean task = Boolean.parseBoolean(metadata.get("task"));
                boolean organizationChart = Boolean.parseBoolean(metadata.get("organizationChart"));
                boolean leaveManagement = Boolean.parseBoolean(metadata.get("leaveManagement"));
                boolean timeSheet = Boolean.parseBoolean(metadata.get("timeSheet"));

                String invoiceId = fullSession.getInvoice();

                if (invoiceId != null) {
                    logger.info("Stripe auto-created invoice {}", invoiceId);
                    Invoice invoice = Invoice.retrieve(invoiceId);
                    String invoiceUrl = invoice.getHostedInvoiceUrl();
                    logger.info("Invoice URL {}", invoiceUrl);
                    logger.info("invoive_Id {}", invoiceId);
                    sendInvoiceEmail(email, firstName + " " + lastName, company, plan, price, invoiceUrl,invoiceId);
                } else {
                    logger.warn("No invoice created – check if invoice creation was enabled in Checkout session.");
                }





                // Your logic to register admin
                registerAdmin(firstName, lastName, email, country, noOfEmployees, plan, price, company, password , task , organizationChart,leaveManagement,timeSheet);



                return ResponseEntity.ok("✅ Webhook handled: session completed.");
            }

            return ResponseEntity.ok("ℹ️ Unhandled event type: " + event.getType());

        } catch (SignatureVerificationException e) {
            logger.debug("Invalid Stripe signature {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature: " + e.getMessage());

        } catch (StripeException e) {
            logger.debug("Stripe API error {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Stripe error: " + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook error: " + e.getMessage());
        }
    }


    public void sendInvoiceEmail(String toEmail, String fullName, String company, String plan, double price, String invoiceUrl, String invoiceId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("✅ Subscription Invoice - " + company);
            message.setText("Hello " + fullName + ",\n\n" +
                    "Thank you for subscribing to the " + plan + " plan.\n" +
                    "Amount Paid: £" + price + "\n" +
                    "Company: " + company + "\n\n" +
                    "You can view or download your invoice here:\n" + invoiceUrl + "\n\n" +
                    "Best regards,\nInvoice Id :"+invoiceId+"\nTalent Flow Team");

            mailSender.send(message);
            logger.info("Invoice email sent to {}", toEmail);
        } catch (Exception e) {
            logger.debug("ailed to send invoice email {}", e.getMessage());
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





    // SESSION CREATION WITH CLIENT METADATA
    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, Object>> createCheckoutSessionWithMetadata(@RequestBody ClientDetails clientDetails) throws StripeException {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("plan", clientDetails.getPlan());
        metadata.put("schemaName", clientDetails.getSchemaName());
        metadata.put("price", String.valueOf(clientDetails.getPrice()));
        metadata.put("noOfEmployees", String.valueOf(clientDetails.getNoOfEmployees()));

        logger.info("Creating checkout session with metadata: {}", metadata);

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

        logger.info("Created session: {}", session.getId());
        logger.debug("Session metadata: {}", session.getMetadata());

        return ResponseEntity.ok(response);
    }

    // VERIFY PAYMENT STATUS (OPTIONAL)
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestParam String session_id) throws StripeException {
        logger.info("Verifying payment for session_id: {}", session_id);
        Session session = Session.retrieve(session_id);
        Map<String, Object> response = new HashMap<>();
        response.put("session_id", session_id);
        response.put("session_status", session.getPaymentStatus());

        if (session.getPaymentIntent() != null) {
            PaymentIntent intent = PaymentIntent.retrieve(session.getPaymentIntent());
            response.put("intent_id", intent.getId());
            response.put("intent_status", intent.getStatus());
            response.put("paid", "succeeded".equals(intent.getStatus()));
            logger.info("PaymentIntent {} has status: {}", intent.getId(), intent.getStatus());
        } else {
            response.put("paid", "complete".equals(session.getPaymentStatus()));
            logger.info("Session {} payment status: {}", session_id, session.getPaymentStatus());
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
                logger.warn("Could not deserialize session from webhook event, falling back to manual extraction.");
                // Fallback: manually extract session ID from raw JSON
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(payload);
                String sessionId = rootNode.path("data").path("object").path("id").asText();
                session = Session.retrieve(sessionId);
            }

            logger.info("Webhook received for session: {}", session.getId());

            if (session != null && session.getMetadata() != null) {
                String schemaName = session.getMetadata().get("schemaName");
                String tenantId=schemaName;
                logger.debug("Extracted schemaName: {}", schemaName);
                String country=null;

                if (tenantId!=null) {
                    int index = tenantId.indexOf("_");
                    country = index != -1 ? tenantId.substring(0, index) : tenantId;
                }
                TenantContext.setCountry(country);
                TenantContext.setTenantId("public");
                logger.info("Tenant country: {}, tenantId set to 'public'", country);

                if (schemaName != null) {
                    ClientDetails clientDetails = clientDetailsService.getClientDetailsBySchema(schemaName);
                    double paidAmount=Double.parseDouble(session.getMetadata().get("price"))/100;
                    clientDetails.setPrice(clientDetails.getPrice()+paidAmount);
                    clientDetails.setNoOfEmployees(Integer.parseInt(session.getMetadata().get("noOfEmployees")));
                    clientDetails.setPlan(session.getMetadata().get("plan"));
                    clientDetailsService.updateClientDetails(clientDetails.getId(), clientDetails);

                    logger.info("Client details updated for schema: {}", schemaName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error processing Stripe webhook", e);
            return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
        }

        return ResponseEntity.ok("Webhook received");
    }


}
