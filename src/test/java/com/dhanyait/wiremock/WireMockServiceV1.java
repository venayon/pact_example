package com.dhanyait.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * WireMock Service for Citizen Address API
 *
 * Provides mock stubs for testing the Citizen Address Service endpoints:
 * - GET /citizen/{cid}/address
 * - POST /citizen/{cid}/address
 *
 * All stub responses are externalized to JSON files in test/resources/stubs/
 *
 * Supports multiple HTTP status codes: 200, 201, 400, 404, 500
 */
public abstract class WireMockServiceV1 {

    private static WireMockServer wireMockServer;
    private static final String STUBS_BASE_PATH = "src/test/resources/stubs";

    @BeforeAll
    public static void startIfRequired() {
        String baseUrl = System.getProperty("baseUrl", "");

        // Only start WireMock when mock profile is selected
        if (!baseUrl.equals("http://localhost:9090")) {
            return;
        }

        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(
                    WireMockConfiguration.options().port(9090)
            );
            wireMockServer.start();
            setupStubs();
        }
    }

    @AfterAll
    public static void stopIfRunning() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    /**
     * Helper method to read JSON content from external stub files
     */
    private static String readStubFile(String filePath) {
        try {
            return Files.readString(Paths.get(STUBS_BASE_PATH, filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stub file: " + filePath, e);
        }
    }

    private static void setupStubs() {

        // ========================================================================
        // GET /citizen/{cid}/address - SUCCESS (200)
        // ========================================================================

        /**
         * Stub: GET citizen address - Success
         * Citizen ID: CID123456
         * Response File: get/get-address-success-CID123456.json
         * Returns: Complete address details with UK address (Flat 5B, SW1A 1AA)
         * Status: 200 OK
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID123456/address"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("get/get-address-success-CID123456.json"))
                ));

        // ========================================================================
        // GET /citizen/{cid}/address - NOT FOUND (404)
        // ========================================================================

        /**
         * Stub: GET citizen address - Not Found
         * Citizen ID: CID999999 (non-existent)
         * Response File: get/get-address-notfound-CID999999.json
         * Returns: Error message indicating citizen not found
         * Status: 404 Not Found
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID999999/address"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("get/get-address-notfound-CID999999.json"))
                ));

        // ========================================================================
        // GET /citizen/{cid}/address - INTERNAL SERVER ERROR (500)
        // ========================================================================

        /**
         * Stub: GET citizen address - Server Error
         * Citizen ID: CID777777
         * Response File: get/get-address-servererror-CID777777.json
         * Returns: Internal server error message
         * Status: 500 Internal Server Error
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID777777/address"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("get/get-address-servererror-CID777777.json"))
                ));

        // ========================================================================
        // POST /citizen/{cid}/address - SUCCESS (201)
        // ========================================================================

        /**
         * Stub: POST create citizen address - Success
         * Citizen ID: CID123456
         * Request File: post/post-address-success-request.json
         * Response File: post/post-address-success-response.json
         * Request: Valid UK address (221B Baker Street, London)
         * Returns: Created address with generated metadata
         * Status: 201 Created
         */
        wireMockServer.stubFor(post(urlEqualTo("/citizen/CID123456/address"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .withRequestBody(matchingJsonPath("$.address_lines"))
                .withRequestBody(matchingJsonPath("$.postcode"))
                .withRequestBody(matchingJsonPath("$.country_code"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("post/post-address-success-response.json"))
                ));

        // ========================================================================
        // POST /citizen/{cid}/address - BAD REQUEST (400)
        // ========================================================================

        /**
         * Stub: POST create citizen address - Bad Request
         * Citizen ID: CID400BAD
         * Request File: post/post-address-badrequest-request.json
         * Response File: post/post-address-badrequest-response.json
         * Request: Invalid payload (missing required postcode field)
         * Returns: Validation error details
         * Status: 400 Bad Request
         *
         * Note: Uses separate endpoint to distinguish from valid requests
         */
        wireMockServer.stubFor(post(urlEqualTo("/citizen/CID400BAD/address"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .withRequestBody(matchingJsonPath("$.address_lines"))
                .withRequestBody(matchingJsonPath("$.country_code"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("post/post-address-badrequest-response.json"))
                ));

        // ========================================================================
        // POST /citizen/{cid}/address - NOT FOUND (404)
        // ========================================================================

        /**
         * Stub: POST create citizen address - Not Found
         * Citizen ID: CID888888 (non-existent)
         * Request File: post/post-address-notfound-request.json
         * Response File: post/post-address-notfound-response.json
         * Request: Valid address payload (Edinburgh Castle)
         * Returns: Error message indicating citizen not found
         * Status: 404 Not Found
         */
        wireMockServer.stubFor(post(urlEqualTo("/citizen/CID888888/address"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("post/post-address-notfound-response.json"))
                ));

        // ========================================================================
        // POST /citizen/{cid}/address - INTERNAL SERVER ERROR (500)
        // ========================================================================

        /**
         * Stub: POST create citizen address - Server Error
         * Citizen ID: CID555555
         * Request File: post/post-address-servererror-request.json
         * Response File: post/post-address-servererror-response.json
         * Request: Valid address payload (Buckingham Palace)
         * Returns: Internal server error message
         * Status: 500 Internal Server Error
         */
        wireMockServer.stubFor(post(urlEqualTo("/citizen/CID555555/address"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("post/post-address-servererror-response.json"))
                ));

        // ========================================================================
        // ADDITIONAL STUBS - Multiple UK Address Examples
        // ========================================================================

        /**
         * Stub: GET - 10 Downing Street address
         * Citizen ID: CID200001
         * Response File: get/get-address-downingstreet-CID200001.json
         * Famous address: UK Prime Minister's residence
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID200001/address"))
                .withHeader("Subsystem-Id", matching(".*"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("get/get-address-downingstreet-CID200001.json"))
                ));

        /**
         * Stub: GET - Edinburgh Castle address
         * Citizen ID: CID200002
         * Response File: get/get-address-edinburghcastle-CID200002.json
         * Famous address: Historic Scottish castle
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID200002/address"))
                .withHeader("Subsystem-Id", matching(".*"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("get/get-address-edinburghcastle-CID200002.json"))
                ));

        /**
         * Stub: GET - Cardiff City Hall address
         * Citizen ID: CID200003
         * Response File: get/get-address-cardiffcityhall-CID200003.json
         * Famous address: Welsh government building
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID200003/address"))
                .withHeader("Subsystem-Id", matching(".*"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("get/get-address-cardiffcityhall-CID200003.json"))
                ));

        /**
         * Stub: GET - Belfast City Hall address
         * Citizen ID: CID200004
         * Response File: get/get-address-belfastcityhall-CID200004.json
         * Famous address: Northern Ireland civic building
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID200004/address"))
                .withHeader("Subsystem-Id", matching(".*"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("get/get-address-belfastcityhall-CID200004.json"))
                ));

        // ========================================================================
        // LEGACY UK ADDRESS FORMAT - 5 Address Lines
        // ========================================================================

        /**
         * Stub: GET - UK Legacy Address Format (5 lines)
         * Citizen ID: CID300001
         * Response File: get/get-address-legacy5lines-CID300001.json
         * Legacy UK address format with full 5-line structure:
         * - Line 1: Apartment/Flat number
         * - Line 2: Building name
         * - Line 3: Street number and name
         * - Line 4: District/Area
         * - Line 5: City/Town
         * Example: Apartment 12, The Royal Chambers, 45 Victoria Street, Westminster, London
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID300001/address"))
                .withHeader("Subsystem-Id", matching(".*"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("get/get-address-legacy5lines-CID300001.json"))
                ));

        /**
         * Stub: POST - Create UK Legacy Address Format (5 lines)
         * Citizen ID: CID300002
         * Request File: post/post-address-legacy5lines-request.json
         * Response File: post/post-address-legacy5lines-response.json
         * Creates address with full 5-line UK legacy format
         * Example: Flat 7C, Windsor Court, 123 Kensington Road, Kensington, London
         */
        wireMockServer.stubFor(post(urlEqualTo("/citizen/CID300002/address"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .withRequestBody(matchingJsonPath("$.address_lines"))
                .withRequestBody(matchingJsonPath("$.postcode"))
                .withRequestBody(matchingJsonPath("$.country_code"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStubFile("post/post-address-legacy5lines-response.json"))
                ));
    }
}