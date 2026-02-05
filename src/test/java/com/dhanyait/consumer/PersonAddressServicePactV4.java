package com.dhanyait.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.dhanyait.wiremock.WireMockServiceV1;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Consumer Contract Tests for Citizen Address Service
 *
 * This class defines Pact contracts between the consumer (PersonAddressConsumer)
 * and provider (PersonAddressService) for citizen address management operations.
 *
 * Endpoints tested:
 * - GET /citizen/{cid}/address - Retrieve citizen address details
 * - POST /citizen/{cid}/address - Create new citizen address
 *
 * Status codes covered:
 * - 200 OK: Successful GET request
 * - 201 Created: Successful POST request
 * - 400 Bad Request: Invalid request payload
 * - 404 Not Found: Citizen not found
 * - 500 Internal Server Error: Server-side failures
 */
@ExtendWith(PactConsumerTestExt.class)
@Epic("Citizen Address Service")
@Feature("API Contract Testing")
@DisplayName("Citizen Address Service - Contract Tests")
public class PersonAddressServicePactV4 extends WireMockServiceV1 {

    // ============================================
    // GET /citizen/{cid}/address - SUCCESS (200)
    // ============================================

    /**
     * Pact: GET request returns address successfully (200 OK)
     *
     * Scenario: Consumer requests address for an existing citizen
     * Given: Citizen with ID "CID123456" exists with valid UK address
     * Expected: Returns 200 with complete address details including timeliness metadata
     */
    @Pact(consumer = "PersonAddressConsumerV4", provider = "PersonAddressService")
    public V4Pact getAddressSuccess(PactDslWithProvider builder) {

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.array("data", arr -> {
                arr.stringValue("Flat 5B");
            });
            o.stringType("postcode", "SW1A 1AA");
            o.stringType("addressTypeId", "RESIDENTIAL");
            o.stringType("addressStatusType", "CURRENT");
            o.object("timeliness", timeliness -> {
                timeliness.stringMatcher("createdDate",
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z",
                        "2024-01-15T10:30:45.123Z");
                timeliness.stringMatcher("lastUpdateDate",
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z",
                        "2024-02-01T14:20:30.456Z");
            });
        }).build();

        return builder
                .given("Citizen with ID CID123456 exists with address")
                .uponReceiving("A request to get citizen address")
                .path("/citizen/CID123456/address")
                .method("GET")
                .headers("Subsystem-Id", "CONSUMER_SYSTEM_001", "Correlation-Id", "corr-12345-abcde")
                .willRespondWith()
                .status(200)
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    /**
     * Test: Verify successful GET request returns citizen address
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "getAddressSuccess", pactVersion = PactSpecVersion.V4)
    @DisplayName("GET /citizen/{cid}/address - Returns 200 with address details")
    @Description("Verifies that consumer can successfully retrieve address for existing citizen")
    @Story("Get Citizen Address - Success")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("QA Team")
    void testGetAddressSuccess(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(mockServer.getUrl() + "/citizen/CID123456/address");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Subsystem-Id", "CONSUMER_SYSTEM_001")
                .header("Correlation-Id", "corr-12345-abcde")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(200, response.statusCode(), "Should return 200 OK");
        assertTrue(response.body().contains("\"data\""), "Response should contain data array");
        assertTrue(response.body().contains("\"postcode\""), "Response should contain postcode");
        assertTrue(response.body().contains("SW1A 1AA"), "Response should contain UK postcode");
        assertTrue(response.body().contains("\"timeliness\""), "Response should contain timeliness");
    }

    // ============================================
    // GET /citizen/{cid}/address - NOT FOUND (404)
    // ============================================

    /**
     * Pact: GET request for non-existent citizen returns 404
     *
     * Scenario: Consumer requests address for a citizen that doesn't exist
     * Given: Citizen with ID "CID999999" does not exist
     * Expected: Returns 404 with error details
     */
    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact getAddressNotFound(PactDslWithProvider builder) {

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.stringType("error", "NOT_FOUND");
            o.stringType("message", "Citizen with ID CID999999 not found");
            o.stringType("timestamp", "2024-02-04T12:00:00.000Z");
        }).build();

        return builder
                .given("Citizen with ID CID999999 does not exist")
                .uponReceiving("A request to get address for non-existent citizen")
                .path("/citizen/CID999999/address")
                .method("GET")
                .headers("Subsystem-Id", "CONSUMER_SYSTEM_001", "Correlation-Id", "corr-67890-fghij")
                .willRespondWith()
                .status(404)
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    /**
     * Test: Verify GET request returns 404 for non-existent citizen
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "getAddressNotFound", pactVersion = PactSpecVersion.V4)
    @DisplayName("GET /citizen/{cid}/address - Returns 404 for non-existent citizen")
    @Description("Verifies that consumer receives 404 when requesting address for non-existent citizen")
    @Story("Get Citizen Address - Not Found")
    @Severity(SeverityLevel.NORMAL)
    @Owner("QA Team")
    void testGetAddressNotFound(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(mockServer.getUrl() + "/citizen/CID999999/address");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Subsystem-Id", "CONSUMER_SYSTEM_001")
                .header("Correlation-Id", "corr-67890-fghij")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(404, response.statusCode(), "Should return 404 Not Found");
        assertTrue(response.body().contains("\"error\""), "Response should contain error field");
        assertTrue(response.body().contains("NOT_FOUND"), "Error should be NOT_FOUND");
    }

    // ============================================
    // GET /citizen/{cid}/address - SERVER ERROR (500)
    // ============================================

    /**
     * Pact: GET request encounters internal server error (500)
     *
     * Scenario: Provider experiences internal error while processing request
     * Given: Service encounters database connectivity issues
     * Expected: Returns 500 with error details
     */
    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact getAddressServerError(PactDslWithProvider builder) {

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.stringType("error", "INTERNAL_SERVER_ERROR");
            o.stringType("message", "An unexpected error occurred while processing the request");
            o.stringType("timestamp", "2024-02-04T12:00:00.000Z");
        }).build();

        return builder
                .given("Service is experiencing internal errors")
                .uponReceiving("A request to get address when service has internal error")
                .path("/citizen/CID777777/address")
                .method("GET")
                .headers("Subsystem-Id", "CONSUMER_SYSTEM_001", "Correlation-Id", "corr-error-11111")
                .willRespondWith()
                .status(500)
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    /**
     * Test: Verify GET request handles 500 server error gracefully
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "getAddressServerError", pactVersion = PactSpecVersion.V4)
    @DisplayName("GET /citizen/{cid}/address - Returns 500 on server error")
    @Description("Verifies that consumer handles server errors appropriately")
    @Story("Get Citizen Address - Server Error")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("QA Team")
    void testGetAddressServerError(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(mockServer.getUrl() + "/citizen/CID777777/address");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Subsystem-Id", "CONSUMER_SYSTEM_001")
                .header("Correlation-Id", "corr-error-11111")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(500, response.statusCode(), "Should return 500 Internal Server Error");
        assertTrue(response.body().contains("\"error\""), "Response should contain error field");
        assertTrue(response.body().contains("INTERNAL_SERVER_ERROR"), "Error should indicate server error");
    }

    // ============================================
    // POST /citizen/{cid}/address - SUCCESS (201)
    // ============================================

    /**
     * Pact: POST request creates address successfully (201 Created)
     *
     * Scenario: Consumer creates new address for existing citizen
     * Given: Citizen with ID "CID123456" exists and can have addresses added
     * Expected: Returns 201 with created address details including generated metadata
     *
     * Sample UK Address: 221B Baker Street, London
     */
    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact postAddressSuccess(PactDslWithProvider builder) {

        var requestBody = LambdaDsl.newJsonBody(o -> {
            o.array("address_lines", arr -> {
                arr.stringValue("221B Baker Street");
            });
            o.stringType("postcode", "NW1 6XE");
            o.stringType("country_code", "GB");
        }).build();

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.array("data", arr -> {
                arr.stringValue("221B Baker Street");
            });
            o.stringType("postcode", "NW1 6XE");
            o.stringType("addressTypeId", "RESIDENTIAL");
            o.stringType("addressStatusType", "CURRENT");
            o.object("timeliness", timeliness -> {
                timeliness.stringMatcher("createdDate",
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z",
                        "2024-02-04T15:30:00.000Z");
                timeliness.stringMatcher("lastUpdateDate",
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z",
                        "2024-02-04T15:30:00.000Z");
            });
        }).build();

        return builder
                .given("Citizen with ID CID123456 exists and can add address")
                .uponReceiving("A request to create a new address")
                .path("/citizen/CID123456/address")
                .method("POST")
                .headers("Content-Type", "application/json", "Subsystem-Id", "CONSUMER_SYSTEM_001", "Correlation-Id", "corr-create-22222")
                .body(requestBody)
                .willRespondWith()
                .status(201)
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    /**
     * Test: Verify successful POST request creates citizen address
     *
     * Sample Data: Famous UK address - 221B Baker Street, London (Sherlock Holmes residence)
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "postAddressSuccess", pactVersion = PactSpecVersion.V4)
    @DisplayName("POST /citizen/{cid}/address - Returns 201 with created address")
    @Description("Verifies that consumer can successfully create new address for existing citizen")
    @Story("Create Citizen Address - Success")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("QA Team")
    void testPostAddressSuccess(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(mockServer.getUrl() + "/citizen/CID123456/address");

        // Request payload with UK address: 221B Baker Street, London
        String requestJson = """
            {
              "address_lines": ["221B Baker Street"],
              "postcode": "NW1 6XE",
              "country_code": "GB"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Subsystem-Id", "CONSUMER_SYSTEM_001")
                .header("Correlation-Id", "corr-create-22222")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(201, response.statusCode(), "Should return 201 Created");
        assertTrue(response.body().contains("\"data\""), "Response should contain data array");
        assertTrue(response.body().contains("\"postcode\""), "Response should contain postcode");
        assertTrue(response.body().contains("NW1 6XE"), "Response should contain the UK postcode");
        assertTrue(response.body().contains("\"timeliness\""), "Response should contain timeliness");
        assertTrue(response.body().contains("\"createdDate\""), "Response should contain creation date");
    }

    // ============================================
    // POST /citizen/{cid}/address - BAD REQUEST (400)
    // ============================================

    /**
     * Pact: POST request with invalid payload returns 400
     *
     * Scenario: Consumer sends request with missing required fields
     * Given: Request body is missing required 'postcode' field
     * Expected: Returns 400 with validation error details
     */
    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact postAddressBadRequest(PactDslWithProvider builder) {

        var requestBody = LambdaDsl.newJsonBody(o -> {
            o.array("address_lines", arr -> {
                arr.stringValue("10 Downing Street");
            });
            // Missing required postcode field
            o.stringType("country_code", "GB");
        }).build();

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.stringType("error", "BAD_REQUEST");
            o.stringType("message", "Missing required field: postcode");
            o.stringType("timestamp", "2024-02-04T12:00:00.000Z");
            o.array("validationErrors", arr -> {
                arr.stringValue("postcode is required");
            });
        }).build();

        return builder
                .given("Service validates request payloads")
                .uponReceiving("A request to create address with missing required field")
                .path("/citizen/CID400BAD/address")
                .method("POST")
                .headers("Content-Type", "application/json", "Subsystem-Id", "CONSUMER_SYSTEM_001", "Correlation-Id", "corr-bad-33333")
                .body(requestBody)
                .willRespondWith()
                .status(400)
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    /**
     * Test: Verify POST request returns 400 for invalid payload
     *
     * Sample Data: 10 Downing Street (UK Prime Minister's residence) - missing postcode
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "postAddressBadRequest", pactVersion = PactSpecVersion.V4)
    @DisplayName("POST /citizen/{cid}/address - Returns 400 for invalid payload")
    @Description("Verifies that consumer receives 400 when sending invalid request payload")
    @Story("Create Citizen Address - Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @Owner("QA Team")
    void testPostAddressBadRequest(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(mockServer.getUrl() + "/citizen/CID400BAD/address");

        // Invalid request: missing required postcode field
        String requestJson = """
            {
              "address_lines": ["10 Downing Street"],
              "country_code": "GB"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Subsystem-Id", "CONSUMER_SYSTEM_001")
                .header("Correlation-Id", "corr-bad-33333")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(400, response.statusCode(), "Should return 400 Bad Request");
        assertTrue(response.body().contains("\"error\""), "Response should contain error field");
        assertTrue(response.body().contains("BAD_REQUEST"), "Error should be BAD_REQUEST");
        assertTrue(response.body().contains("validationErrors"), "Response should contain validation errors");
    }

    // ============================================
    // POST /citizen/{cid}/address - NOT FOUND (404)
    // ============================================

    /**
     * Pact: POST request for non-existent citizen returns 404
     *
     * Scenario: Consumer attempts to create address for non-existent citizen
     * Given: Citizen with ID "CID888888" does not exist
     * Expected: Returns 404 with error details
     *
     * Sample UK Address: Edinburgh Castle, Scotland
     */
    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact postAddressNotFound(PactDslWithProvider builder) {

        var requestBody = LambdaDsl.newJsonBody(o -> {
            o.array("address_lines", arr -> {
                arr.stringValue("Castlehill");
            });
            o.stringType("postcode", "EH1 2NG");
            o.stringType("country_code", "GB");
        }).build();

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.stringType("error", "NOT_FOUND");
            o.stringType("message", "Citizen with ID CID888888 not found");
            o.stringType("timestamp", "2024-02-04T12:00:00.000Z");
        }).build();

        return builder
                .given("Citizen with ID CID888888 does not exist")
                .uponReceiving("A request to create address for non-existent citizen")
                .path("/citizen/CID888888/address")
                .method("POST")
                .headers("Content-Type", "application/json", "Subsystem-Id", "CONSUMER_SYSTEM_001", "Correlation-Id", "corr-notfound-44444")
                .body(requestBody)
                .willRespondWith()
                .status(404)
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    /**
     * Test: Verify POST request returns 404 for non-existent citizen
     *
     * Sample Data: Edinburgh Castle address, Scotland
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "postAddressNotFound", pactVersion = PactSpecVersion.V4)
    @DisplayName("POST /citizen/{cid}/address - Returns 404 for non-existent citizen")
    @Description("Verifies that consumer receives 404 when creating address for non-existent citizen")
    @Story("Create Citizen Address - Not Found")
    @Severity(SeverityLevel.NORMAL)
    @Owner("QA Team")
    void testPostAddressNotFound(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(mockServer.getUrl() + "/citizen/CID888888/address");

        // Request payload with Edinburgh Castle address
        String requestJson = """
            {
              "address_lines": ["Castlehill"],
              "postcode": "EH1 2NG",
              "country_code": "GB"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Subsystem-Id", "CONSUMER_SYSTEM_001")
                .header("Correlation-Id", "corr-notfound-44444")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(404, response.statusCode(), "Should return 404 Not Found");
        assertTrue(response.body().contains("\"error\""), "Response should contain error field");
        assertTrue(response.body().contains("NOT_FOUND"), "Error should be NOT_FOUND");
        assertTrue(response.body().contains("CID888888"), "Error message should reference citizen ID");
    }

    // ============================================
    // POST /citizen/{cid}/address - SERVER ERROR (500)
    // ============================================

    /**
     * Pact: POST request encounters internal server error (500)
     *
     * Scenario: Provider experiences internal error while creating address
     * Given: Service encounters database write failure
     * Expected: Returns 500 with error details
     *
     * Sample UK Address: Buckingham Palace, London
     */
    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact postAddressServerError(PactDslWithProvider builder) {

        var requestBody = LambdaDsl.newJsonBody(o -> {
            o.array("address_lines", arr -> {
                arr.stringValue("Buckingham Palace");
            });
            o.stringType("postcode", "SW1A 1AA");
            o.stringType("country_code", "GB");
        }).build();

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.stringType("error", "INTERNAL_SERVER_ERROR");
            o.stringType("message", "Failed to create address due to internal error");
            o.stringType("timestamp", "2024-02-04T12:00:00.000Z");
        }).build();

        return builder
                .given("Service is experiencing database write failures")
                .uponReceiving("A request to create address when service has internal error")
                .path("/citizen/CID555555/address")
                .method("POST")
                .headers("Content-Type", "application/json", "Subsystem-Id", "CONSUMER_SYSTEM_001", "Correlation-Id", "corr-error-55555")
                .body(requestBody)
                .willRespondWith()
                .status(500)
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    /**
     * Test: Verify POST request handles 500 server error gracefully
     *
     * Sample Data: Buckingham Palace address, London
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "postAddressServerError", pactVersion = PactSpecVersion.V4)
    @DisplayName("POST /citizen/{cid}/address - Returns 500 on server error")
    @Description("Verifies that consumer handles server errors appropriately during address creation")
    @Story("Create Citizen Address - Server Error")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("QA Team")
    void testPostAddressServerError(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(mockServer.getUrl() + "/citizen/CID555555/address");

        // Request payload with Buckingham Palace address
        String requestJson = """
            {
              "address_lines": ["Buckingham Palace"],
              "postcode": "SW1A 1AA",
              "country_code": "GB"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Subsystem-Id", "CONSUMER_SYSTEM_001")
                .header("Correlation-Id", "corr-error-55555")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(500, response.statusCode(), "Should return 500 Internal Server Error");
        assertTrue(response.body().contains("\"error\""), "Response should contain error field");
        assertTrue(response.body().contains("INTERNAL_SERVER_ERROR"), "Error should indicate server error");
    }

    // ============================================
    // GET /citizen/{cid}/address - LEGACY UK ADDRESS (5 lines) - SUCCESS (200)
    // ============================================

    /**
     * Pact: GET request returns UK legacy address with 5 address lines
     *
     * Scenario: Consumer requests legacy UK address format with full 5-line structure
     * Given: Citizen with ID "CID300001" exists with legacy 5-line UK address
     * Expected: Returns 200 with address in format:
     *   Line 1: Apartment/Flat number
     *   Line 2: Building name
     *   Line 3: Street number and name
     *   Line 4: District/Area
     *   Line 5: City/Town
     * Example: Apartment 12, The Royal Chambers, 45 Victoria Street, Westminster, London
     */
    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact getAddressLegacy5Lines(PactDslWithProvider builder) {

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.array("data", arr -> {
                arr.stringValue("Apartment 12");
                arr.stringValue("The Royal Chambers");
                arr.stringValue("45 Victoria Street");
                arr.stringValue("Westminster");
                arr.stringValue("London");
            });
            o.stringType("postcode", "SW1H 0NW");
            o.stringType("addressTypeId", "RESIDENTIAL");
            o.stringType("addressStatusType", "CURRENT");
            o.object("timeliness", timeliness -> {
                timeliness.stringMatcher("createdDate",
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z",
                        "2023-09-20T11:15:30.000Z");
                timeliness.stringMatcher("lastUpdateDate",
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z",
                        "2024-01-10T09:45:20.000Z");
            });
        }).build();

        return builder
                .given("Citizen with ID CID300001 exists with legacy 5-line address")
                .uponReceiving("A request to get citizen address in legacy UK format")
                .path("/citizen/CID300001/address")
                .method("GET")
                .headers("Subsystem-Id", "CONSUMER_SYSTEM_001", "Correlation-Id", "corr-legacy-11111")
                .willRespondWith()
                .status(200)
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    /**
     * Test: Verify successful GET request returns UK legacy 5-line address
     *
     * Sample Data: The Royal Chambers, Westminster, London (5-line format)
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "getAddressLegacy5Lines", pactVersion = PactSpecVersion.V4)
    @DisplayName("GET /citizen/{cid}/address - Returns 200 with legacy 5-line UK address")
    @Description("Verifies that consumer can successfully retrieve legacy UK address format with 5 address lines")
    @Story("Get Citizen Address - Legacy UK Format")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("QA Team")
    void testGetAddressLegacy5Lines(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(mockServer.getUrl() + "/citizen/CID300001/address");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Subsystem-Id", "CONSUMER_SYSTEM_001")
                .header("Correlation-Id", "corr-legacy-11111")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(200, response.statusCode(), "Should return 200 OK");
        assertTrue(response.body().contains("\"data\""), "Response should contain data array");
        assertTrue(response.body().contains("Apartment 12"), "Response should contain apartment number");
        assertTrue(response.body().contains("The Royal Chambers"), "Response should contain building name");
        assertTrue(response.body().contains("45 Victoria Street"), "Response should contain street");
        assertTrue(response.body().contains("Westminster"), "Response should contain district");
        assertTrue(response.body().contains("London"), "Response should contain city");
        assertTrue(response.body().contains("SW1H 0NW"), "Response should contain UK postcode");
    }

    // ============================================
    // POST /citizen/{cid}/address - LEGACY UK ADDRESS (5 lines) - SUCCESS (201)
    // ============================================

    /**
     * Pact: POST request creates UK legacy address with 5 address lines
     *
     * Scenario: Consumer creates new address in legacy UK 5-line format
     * Given: Citizen with ID "CID300002" exists and can have addresses added
     * Expected: Returns 201 with created address in 5-line format
     *
     * Sample UK Legacy Address: Flat 7C, Windsor Court, 123 Kensington Road, Kensington, London
     */
    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact postAddressLegacy5Lines(PactDslWithProvider builder) {

        var requestBody = LambdaDsl.newJsonBody(o -> {
            o.array("address_lines", arr -> {
                arr.stringValue("Flat 7C");
                arr.stringValue("Windsor Court");
                arr.stringValue("123 Kensington Road");
                arr.stringValue("Kensington");
                arr.stringValue("London");
            });
            o.stringType("postcode", "W8 5SA");
            o.stringType("country_code", "GB");
        }).build();

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.array("data", arr -> {
                arr.stringValue("Flat 7C");
                arr.stringValue("Windsor Court");
                arr.stringValue("123 Kensington Road");
                arr.stringValue("Kensington");
                arr.stringValue("London");
            });
            o.stringType("postcode", "W8 5SA");
            o.stringType("addressTypeId", "RESIDENTIAL");
            o.stringType("addressStatusType", "CURRENT");
            o.object("timeliness", timeliness -> {
                timeliness.stringMatcher("createdDate",
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z",
                        "2024-02-04T16:20:00.000Z");
                timeliness.stringMatcher("lastUpdateDate",
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z",
                        "2024-02-04T16:20:00.000Z");
            });
        }).build();

        return builder
                .given("Citizen with ID CID300002 exists and can add legacy 5-line address")
                .uponReceiving("A request to create a new address in legacy UK 5-line format")
                .path("/citizen/CID300002/address")
                .method("POST")
                .headers("Content-Type", "application/json", "Subsystem-Id", "CONSUMER_SYSTEM_001", "Correlation-Id", "corr-legacy-22222")
                .body(requestBody)
                .willRespondWith()
                .status(201)
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    /**
     * Test: Verify successful POST request creates UK legacy 5-line address
     *
     * Sample Data: Flat 7C, Windsor Court, Kensington, London (5-line format)
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "postAddressLegacy5Lines", pactVersion = PactSpecVersion.V4)
    @DisplayName("POST /citizen/{cid}/address - Returns 201 with legacy 5-line UK address")
    @Description("Verifies that consumer can successfully create new address in legacy UK 5-line format")
    @Story("Create Citizen Address - Legacy UK Format")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("QA Team")
    void testPostAddressLegacy5Lines(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(mockServer.getUrl() + "/citizen/CID300002/address");

        // Request payload with UK legacy 5-line address format
        String requestJson = """
            {
              "address_lines": [
                "Flat 7C",
                "Windsor Court",
                "123 Kensington Road",
                "Kensington",
                "London"
              ],
              "postcode": "W8 5SA",
              "country_code": "GB"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Subsystem-Id", "CONSUMER_SYSTEM_001")
                .header("Correlation-Id", "corr-legacy-22222")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assertions
        assertEquals(201, response.statusCode(), "Should return 201 Created");
        assertTrue(response.body().contains("\"data\""), "Response should contain data array");
        assertTrue(response.body().contains("Flat 7C"), "Response should contain flat number");
        assertTrue(response.body().contains("Windsor Court"), "Response should contain building name");
        assertTrue(response.body().contains("123 Kensington Road"), "Response should contain street");
        assertTrue(response.body().contains("Kensington"), "Response should contain district");
        assertTrue(response.body().contains("London"), "Response should contain city");
        assertTrue(response.body().contains("W8 5SA"), "Response should contain UK postcode");
        assertTrue(response.body().contains("\"timeliness\""), "Response should contain timeliness");
    }
}