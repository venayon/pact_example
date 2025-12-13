package com.dhanyait.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;

import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.dhanyait.wiremock.BaseWireMockTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PactConsumerTestExt.class)
@Epic("Person Address Service")
@Feature("API Contract Testing")
@DisplayName("Person Address Service - Contract Tests")
public class PersonAddressServicePact extends BaseWireMockTest {

    // ============================================
    // GET /persons/{id} — Existing Pact (keep yours)
    // ============================================

    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact createGetPersonPact(PactDslWithProvider builder) {

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.stringType("person_id", "12345");
            o.stringType("first_name", "John");
            o.stringType("last_name", "Doe");
            o.stringMatcher("dob", "\\d{4}-\\d{2}-\\d{2}", "1980-01-01");
        }).build();

        return builder
                .given("Person with ID 12345 exists")
                .uponReceiving("A request to fetch person details")
                .path("/persons/12345")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(responseBody)
                .toPact(V4Pact.class);
    }


    // ============================================
    // POST /persons — NEW Pact
    // ============================================
    @Pact(consumer = "PersonAddressConsumer", provider = "PersonAddressService")
    public V4Pact createPostPersonPact(PactDslWithProvider builder) {

        var requestBody = LambdaDsl.newJsonBody(o -> {
            o.stringType("first_name", "Alex");
            o.stringType("last_name", "Brown");
            o.stringMatcher("dob", "\\d{4}-\\d{2}-\\d{2}", "1990-05-10");
        }).build();

        var responseBody = LambdaDsl.newJsonBody(o -> {
            o.stringType("person_id", "98765");
            o.stringType("first_name", "Alex");
            o.stringType("last_name", "Brown");
            o.stringMatcher("dob", "\\d{4}-\\d{2}-\\d{2}", "1990-05-10");
        }).build();

        return builder
                .given("Person can be created")
                .uponReceiving("A request to create a new person record")
                .path("/persons")
                .method("POST")
                .headers("Content-Type", "application/json")
                .body(requestBody)
                .willRespondWith()
                .status(201)
                //.headers("Content-Type", "application/json")
                .body(responseBody)
                .toPact(V4Pact.class);
    }


    /**
     * Test GET using Pact Mock Server (for contract generation)
     * This test uses createGetPersonPact and generates the Pact contract file
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "createGetPersonPact", pactVersion = PactSpecVersion.V4)
    @DisplayName("Get Person Details - Contract Verification")
    @Description("Verifies that the consumer can successfully retrieve person details by ID. " +
            "This test validates the contract between PersonAddressConsumer and PersonAddressService " +
            "for the GET /persons/{id} endpoint.")
    @Story("Get Person by ID")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("QA Team")
    void testGetPersonWithPact(MockServer mockServer) throws Exception {
        String baseUrl = mockServer.getUrl();
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create(baseUrl + "/persons/12345");

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(uri)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"first_name\""));
        assertTrue(response.body().contains("\"person_id\""));
    }

    /**
     * Test POST using Pact Mock Server (for contract generation)
     * This test uses createPostPersonPact and generates the Pact contract file
     */
    @Test
    @PactTestFor(providerName = "PersonAddressService", pactMethod = "createPostPersonPact", pactVersion = PactSpecVersion.V4)
    @DisplayName("Create New Person - Contract Verification")
    @Description("Verifies that the consumer can successfully create a new person record. " +
            "This test validates the contract between PersonAddressConsumer and PersonAddressService " +
            "for the POST /persons endpoint.")
    @Story("Create Person")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("QA Team")
    void testPostPersonWithPact(MockServer mockServer) throws Exception {
        String baseUrl = mockServer.getUrl();
        HttpClient client = HttpClient.newHttpClient();

        String reqJson = """
            {
              "first_name": "Alex",
              "last_name": "Brown",
              "dob": "1990-05-10"
            }
            """;
        URI uri = URI.create(baseUrl + "/persons");

        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqJson))
                .build();

        HttpResponse<String> postResp =
                client.send(postReq, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, postResp.statusCode());
        assertTrue(postResp.body().contains("\"person_id\""));
        assertTrue(postResp.body().contains("\"first_name\":\"Alex\""));
    }
}
