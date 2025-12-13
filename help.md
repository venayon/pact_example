## Allure Test Reporting

### Quick Start

**Generate and view report:**
```bash
./generate-allure-report.sh
./serve-allure-report.sh
```

**Generate standalone report for sharing:**
```bash
./generate-standalone-report.sh
```

### Manual Commands

```bash
# Run tests and collect results
mvn clean test -DbaseUrl=http://localhost:9090

# Generate report
mvn allure:report

# Serve report locally (opens in browser)
mvn allure:serve
```

### Report Location

- Generated report: `target/allure-report/index.html`
- Standalone report: `target/allure-standalone-report/`

### For Business/PO

The Allure reports provide:
- Executive summary with pass/fail statistics
- Visual charts and graphs
- Detailed test descriptions
- Test execution timeline
- Filtering by features, stories, and severity

See `ALLURE_REPORTING.md` for detailed documentation.


```java
package com.dhanyait.consumer;

import com.dhanyait.wiremock.BaseWireMockTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test using WireMock (for development before producer is ready)
 * Run with: -DbaseUrl=http://localhost:9090
 * 
 * This test class is separate from Pact tests to avoid Pact validation
 */
@Epic("Person Address Service")
@Feature("API Integration Testing")
@DisplayName("Person Address Service - Integration Tests (WireMock)")
public class PersonAddressServiceWireMockTest extends BaseWireMockTest {

    @Test
    @DisplayName("Person Service Integration Test - Get and Create Person")
    @Description("Integration test using WireMock to verify GET and POST endpoints for Person service. " +
            "This test runs against a mock server and validates the complete flow of retrieving " +
            "and creating person records. Used for development before the actual producer service is ready.")
    @Story("Person Service Integration")
    @Severity(SeverityLevel.NORMAL)
    @Owner("QA Team")
    void testConsumerWithWireMock() throws Exception {
        // Ensure WireMock is running
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            throw new IllegalStateException("WireMock server is not running. Make sure BaseWireMockTest initialized it.");
        }
        
        String baseUrl = System.getProperty("baseUrl", "http://localhost:9090");
        HttpClient client = HttpClient.newHttpClient();

        // ---------- TEST GET ----------
        URI uri = URI.create(baseUrl + "/persons/12345");

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(uri)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"first_name\""), "Response should contain first_name: " + response.body());
        assertTrue(response.body().contains("\"person_id\"") && response.body().contains("12345"), 
                   "Response should contain person_id with value 12345: " + response.body());

        // ---------- TEST POST ----------
        String reqJson = """
            {
              "first_name": "Alex",
              "last_name": "Brown",
              "dob": "1990-05-10"
            }
            """;
        uri = URI.create(baseUrl + "/persons");

        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqJson))
                .build();

        HttpResponse<String> postResp =
                client.send(postReq, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, postResp.statusCode());
        assertTrue(postResp.body().contains("\"person_id\""), "Response should contain person_id: " + postResp.body());
        assertTrue(postResp.body().contains("\"first_name\"") && postResp.body().contains("Alex"), 
                   "Response should contain first_name with value Alex: " + postResp.body());
    }
}


```