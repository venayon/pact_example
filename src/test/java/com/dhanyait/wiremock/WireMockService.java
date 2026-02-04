package com.dhanyait.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * WireMock Service for Citizen Address API
 *
 * Provides mock stubs for testing the Citizen Address Service endpoints:
 * - GET /citizen/{cid}/address
 * - POST /citizen/{cid}/address
 *
 * Supports multiple HTTP status codes: 200, 201, 400, 404, 500
 */
public class WireMockService {

    private static WireMockServer wireMockServer;

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

    public static void stopIfRunning() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    private static void setupStubs() {

        // ========================================================================
        // GET /citizen/{cid}/address - SUCCESS (200)
        // ========================================================================

        /**
         * Stub: GET citizen address - Success
         * Citizen ID: CID123456
         * Returns: Complete address details with UK address (Flat 5B, SW1A 1AA)
         * Status: 200 OK
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID123456/address"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "data": [
                                    "Flat 5B",
                                    "Westminster Palace",
                                    "London"
                                  ],
                                  "postcode": "SW1A 1AA",
                                  "addressTypeId": "RESIDENTIAL",
                                  "addressStatusType": "CURRENT",
                                  "timeliness": {
                                    "createdDate": "2024-01-15T10:30:45.123Z",
                                    "lastUpdateDate": "2024-02-01T14:20:30.456Z"
                                  }
                                }
                                """)
                ));

        // ========================================================================
        // GET /citizen/{cid}/address - NOT FOUND (404)
        // ========================================================================

        /**
         * Stub: GET citizen address - Not Found
         * Citizen ID: CID999999 (non-existent)
         * Returns: Error message indicating citizen not found
         * Status: 404 Not Found
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID999999/address"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "error": "NOT_FOUND",
                                  "message": "Citizen with ID CID999999 not found",
                                  "timestamp": "2024-02-04T12:00:00.000Z"
                                }
                                """)
                ));

        // ========================================================================
        // GET /citizen/{cid}/address - INTERNAL SERVER ERROR (500)
        // ========================================================================

        /**
         * Stub: GET citizen address - Server Error
         * Citizen ID: CID777777
         * Returns: Internal server error message
         * Status: 500 Internal Server Error
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID777777/address"))
                .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "error": "INTERNAL_SERVER_ERROR",
                                  "message": "An unexpected error occurred while processing the request",
                                  "timestamp": "2024-02-04T12:00:00.000Z"
                                }
                                """)
                ));

        // ========================================================================
        // POST /citizen/{cid}/address - SUCCESS (201)
        // ========================================================================

        /**
         * Stub: POST create citizen address - Success
         * Citizen ID: CID123456
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
                        .withBody("""
                                {
                                  "data": [
                                    "221B Baker Street",
                                    "Marylebone",
                                    "London"
                                  ],
                                  "postcode": "NW1 6XE",
                                  "addressTypeId": "RESIDENTIAL",
                                  "addressStatusType": "CURRENT",
                                  "timeliness": {
                                    "createdDate": "2024-02-04T15:30:00.000Z",
                                    "lastUpdateDate": "2024-02-04T15:30:00.000Z"
                                  }
                                }
                                """)
                ));

        // ========================================================================
        // POST /citizen/{cid}/address - BAD REQUEST (400)
        // ========================================================================

        /**
         * Stub: POST create citizen address - Bad Request
         * Citizen ID: CID400BAD
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
                        .withBody("""
                                {
                                  "error": "BAD_REQUEST",
                                  "message": "Missing required field: postcode",
                                  "timestamp": "2024-02-04T12:00:00.000Z",
                                  "validationErrors": [
                                    "postcode is required"
                                  ]
                                }
                                """)
                ));

        // ========================================================================
        // POST /citizen/{cid}/address - NOT FOUND (404)
        // ========================================================================

        /**
         * Stub: POST create citizen address - Not Found
         * Citizen ID: CID888888 (non-existent)
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
                        .withBody("""
                                {
                                  "error": "NOT_FOUND",
                                  "message": "Citizen with ID CID888888 not found",
                                  "timestamp": "2024-02-04T12:00:00.000Z"
                                }
                                """)
                ));

        // ========================================================================
        // POST /citizen/{cid}/address - INTERNAL SERVER ERROR (500)
        // ========================================================================

        /**
         * Stub: POST create citizen address - Server Error
         * Citizen ID: CID555555
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
                        .withBody("""
                                {
                                  "error": "INTERNAL_SERVER_ERROR",
                                  "message": "Failed to create address due to internal error",
                                  "timestamp": "2024-02-04T12:00:00.000Z"
                                }
                                """)
                ));

        // ========================================================================
        // ADDITIONAL STUBS - Multiple UK Address Examples
        // ========================================================================

        /**
         * Stub: GET - 10 Downing Street address
         * Citizen ID: CID200001
         * Famous address: UK Prime Minister's residence
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID200001/address"))
                .withHeader("Subsystem-Id", matching(".*"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "data": [
                                    "10 Downing Street",
                                    "Westminster",
                                    "London"
                                  ],
                                  "postcode": "SW1A 2AA",
                                  "addressTypeId": "GOVERNMENT",
                                  "addressStatusType": "CURRENT",
                                  "timeliness": {
                                    "createdDate": "2023-06-10T09:00:00.000Z",
                                    "lastUpdateDate": "2024-01-20T11:30:00.000Z"
                                  }
                                }
                                """)
                ));

        /**
         * Stub: GET - Edinburgh Castle address
         * Citizen ID: CID200002
         * Famous address: Historic Scottish castle
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID200002/address"))
                .withHeader("Subsystem-Id", matching(".*"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "data": [
                                    "Edinburgh Castle",
                                    "Castlehill",
                                    "Edinburgh"
                                  ],
                                  "postcode": "EH1 2NG",
                                  "addressTypeId": "HISTORIC",
                                  "addressStatusType": "CURRENT",
                                  "timeliness": {
                                    "createdDate": "2023-03-15T14:20:00.000Z",
                                    "lastUpdateDate": "2023-12-01T16:45:00.000Z"
                                  }
                                }
                                """)
                ));

        /**
         * Stub: GET - Cardiff City Hall address
         * Citizen ID: CID200003
         * Famous address: Welsh government building
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID200003/address"))
                .withHeader("Subsystem-Id", matching(".*"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "data": [
                                    "Cardiff City Hall",
                                    "Cathays Park",
                                    "Cardiff"
                                  ],
                                  "postcode": "CF10 3ND",
                                  "addressTypeId": "GOVERNMENT",
                                  "addressStatusType": "CURRENT",
                                  "timeliness": {
                                    "createdDate": "2023-08-22T10:15:00.000Z",
                                    "lastUpdateDate": "2024-01-05T13:20:00.000Z"
                                  }
                                }
                                """)
                ));

        /**
         * Stub: GET - Belfast City Hall address
         * Citizen ID: CID200004
         * Famous address: Northern Ireland civic building
         */
        wireMockServer.stubFor(get(urlEqualTo("/citizen/CID200004/address"))
                .withHeader("Subsystem-Id", matching(".*"))
                .withHeader("Correlation-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "data": [
                                    "Belfast City Hall",
                                    "Donegall Square",
                                    "Belfast"
                                  ],
                                  "postcode": "BT1 5GS",
                                  "addressTypeId": "GOVERNMENT",
                                  "addressStatusType": "CURRENT",
                                  "timeliness": {
                                    "createdDate": "2023-05-18T08:30:00.000Z",
                                    "lastUpdateDate": "2023-11-28T15:10:00.000Z"
                                  }
                                }
                                """)
                ));
    }
}