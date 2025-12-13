package com.dhanyait.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseWireMockTest {

    protected static WireMockServer wireMockServer;

    @BeforeAll
    static void initWireMock() {
        // Start WireMock if not already running
        // WireMock will be used when baseUrl is set to http://localhost:9090
        // or when running tests that extend this base class
        if (wireMockServer == null) {
            System.out.println("Starting WireMock server on port 9090...");
            wireMockServer = new WireMockServer(
                    WireMockConfiguration.options()
                            .port(9090)
                            .usingFilesUnderClasspath("wiremock")
            );
            wireMockServer.start();
            System.out.println("WireMock server started at: http://localhost:9090");
        }
    }

    @AfterAll
    static void shutdownWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }
}
