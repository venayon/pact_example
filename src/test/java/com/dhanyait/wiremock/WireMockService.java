package com.dhanyait.wiremock;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

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

        // ------------------------ GET /persons/12345 ----------------------------
        wireMockServer.stubFor(get(urlEqualTo("/persons/12345"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "person_id": "12345",
                                  "first_name": "John",
                                  "last_name": "Doe",
                                  "dob": "1980-01-01"
                                }
                                """)
                ));

        // ------------------------ POST /persons --------------------------------
        wireMockServer.stubFor(post(urlEqualTo("/persons"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "person_id": "98765",
                                  "first_name": "Alex",
                                  "last_name": "Brown",
                                  "dob": "1990-05-10"
                                }
                                """)
                ));
    }
}

