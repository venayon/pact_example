# WireMock vs Real Service Configuration Guide

## How It Works

The `WireMockService` uses a system property `baseUrl` to determine whether to start the mock server or use the real service.

### Configuration Logic

```java
public static void startIfRequired() {
    String baseUrl = System.getProperty("baseUrl", "");
    
    // Only start WireMock when mock profile is selected
    if (!baseUrl.equals("http://localhost:9090")) {
        return; // WireMock will NOT start
    }
    
    // If baseUrl IS "http://localhost:9090", WireMock starts
    if (wireMockServer == null) {
        wireMockServer = new WireMockServer(
            WireMockConfiguration.options().port(9090)
        );
        wireMockServer.start();
        setupStubs();
    }
}
```

## Execution Modes

### Mode 1: Using WireMock (Mocked Service)

**When to use:** Local development, contract testing, CI/CD pipelines

**Configuration:**
```bash
# Maven
mvn test -DbaseUrl=http://localhost:9090

# Gradle
gradle test -DbaseUrl=http://localhost:9090

# IDE (IntelliJ/Eclipse)
VM Options: -DbaseUrl=http://localhost:9090
```

**Behavior:**
- ✅ WireMock server starts on port 9090
- ✅ All stubs are loaded from `test/resources/stubs/`
- ✅ Tests hit WireMock (predictable, fast, isolated)
- ✅ No network dependency
- ✅ Perfect for contract verification

### Mode 2: Using Real Service

**When to use:** Integration testing, UAT, end-to-end testing

**Configuration:**
```bash
# Maven - pointing to DEV environment
mvn test -DbaseUrl=http://dev-server.company.com:8080

# Maven - pointing to UAT environment
mvn test -DbaseUrl=http://uat-server.company.com:8080

# Gradle - pointing to real service
gradle test -DbaseUrl=http://api.production.com

# IDE (IntelliJ/Eclipse)
VM Options: -DbaseUrl=http://dev-server.company.com:8080
```

**Behavior:**
- ❌ WireMock does NOT start
- ✅ Tests hit the real service
- ✅ Validates actual provider implementation
- ⚠️ Requires network connectivity
- ⚠️ Depends on service availability
- ⚠️ May have test data dependencies

### Mode 3: Default (No Configuration)

**Configuration:**
```bash
# No baseUrl specified
mvn test
```

**Behavior:**
- ❌ WireMock does NOT start (baseUrl defaults to "")
- ⚠️ Tests will fail if they expect a service
- 💡 Best practice: Always specify baseUrl

## Test Execution Flow

```
Test Execution Starts
        │
        ▼
    Check baseUrl
        │
        ├─── baseUrl = "http://localhost:9090"
        │         │
        │         ▼
        │    Start WireMock on port 9090
        │         │
        │         ▼
        │    Load Stubs from JSON files
        │         │
        │         ▼
        │    Tests → WireMock (Mocked Responses)
        │
        └─── baseUrl = "http://real-service.com"
                  │
                  ▼
             Skip WireMock startup
                  │
                  ▼
             Tests → Real Service (Actual Responses)
```

## Maven/Gradle Profiles (Recommended Approach)

### Maven pom.xml

```xml
<profiles>
    <!-- Mock Profile (Default) -->
    <profile>
        <id>mock</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <baseUrl>http://localhost:9090</baseUrl>
        </properties>
    </profile>
    
    <!-- Dev Environment -->
    <profile>
        <id>dev</id>
        <properties>
            <baseUrl>http://dev-api.company.com:8080</baseUrl>
        </properties>
    </profile>
    
    <!-- UAT Environment -->
    <profile>
        <id>uat</id>
        <properties>
            <baseUrl>http://uat-api.company.com:8080</baseUrl>
        </properties>
    </profile>
    
    <!-- Production Environment -->
    <profile>
        <id>prod</id>
        <properties>
            <baseUrl>http://api.production.com</baseUrl>
        </properties>
    </profile>
</profiles>
```

**Usage:**
```bash
# Run with WireMock (default)
mvn test

# Run against DEV
mvn test -Pdev

# Run against UAT
mvn test -Puat

# Run against Production
mvn test -Pprod
```

### Gradle build.gradle

```groovy
test {
    // Default to WireMock
    systemProperty 'baseUrl', project.findProperty('baseUrl') ?: 'http://localhost:9090'
}

// Define tasks for different environments
task testDev(type: Test) {
    systemProperty 'baseUrl', 'http://dev-api.company.com:8080'
}

task testUat(type: Test) {
    systemProperty 'baseUrl', 'http://uat-api.company.com:8080'
}

task testProd(type: Test) {
    systemProperty 'baseUrl', 'http://api.production.com'
}
```

**Usage:**
```bash
# Run with WireMock (default)
gradle test

# Run against specific environment
gradle testDev
gradle testUat
gradle testProd

# Or use property
gradle test -PbaseUrl=http://custom-server.com
```

## Pact Contract Testing Flow

### Consumer Side (Your Tests)

1. **Contract Generation (WireMock)**
   ```bash
   mvn test -DbaseUrl=http://localhost:9090
   ```
   - Runs against Pact Mock Server
   - Generates contract files (pact JSON files)
   - Publishes to Pact Broker

2. **Provider Verification (Real Service)**
   ```bash
   mvn test -DbaseUrl=http://dev-server.com
   ```
   - Provider fetches contract from Pact Broker
   - Runs verification against real provider service
   - Confirms real service matches contract

## CI/CD Pipeline Example

```yaml
# .github/workflows/test.yml
name: Test Pipeline

jobs:
  contract-tests:
    runs-on: ubuntu-latest
    steps:
      - name: Run Pact Consumer Tests
        run: mvn test -DbaseUrl=http://localhost:9090
      
      - name: Publish Pacts
        run: mvn pact:publish
  
  integration-tests:
    runs-on: ubuntu-latest
    needs: contract-tests
    steps:
      - name: Run Integration Tests against DEV
        run: mvn test -DbaseUrl=http://dev-api.company.com:8080
  
  provider-verification:
    runs-on: ubuntu-latest
    needs: contract-tests
    steps:
      - name: Verify Provider Honors Contract
        run: mvn pact:verify -DbaseUrl=http://provider-service.com
```

## Best Practices

1. **Use WireMock for:**
   - ✅ Contract test generation
   - ✅ Fast feedback loops
   - ✅ Isolated unit tests
   - ✅ CI/CD pipelines
   - ✅ Offline development

2. **Use Real Service for:**
   - ✅ Provider verification
   - ✅ Integration testing
   - ✅ End-to-end testing
   - ✅ Pre-production validation

3. **Environment Strategy:**
   - Local: WireMock
   - CI: WireMock for contracts, Real for integration
   - UAT: Real service
   - Production: Limited smoke tests only

## Troubleshooting

### Issue: Tests fail with "Connection refused"

**Cause:** baseUrl is set but service isn't running

**Solutions:**
- If testing locally: Use `-DbaseUrl=http://localhost:9090` (WireMock)
- If testing remote: Ensure service is up and accessible
- Check firewall/network settings

### Issue: WireMock starts when it shouldn't

**Cause:** baseUrl is exactly "http://localhost:9090"

**Solution:** Use different baseUrl for real service testing

### Issue: Stubs not loading

**Cause:** File path is wrong or files don't exist

**Solution:** 
- Verify files exist in `src/test/resources/stubs/`
- Check file paths in `WireMockService.java`
- Review console for "Failed to read stub file" errors

## Summary

| Scenario | baseUrl Value | WireMock Starts? | Tests Hit |
|----------|---------------|------------------|-----------|
| Contract Testing | `http://localhost:9090` | ✅ Yes | WireMock Stubs |
| Dev Environment | `http://dev-api.com` | ❌ No | Real Dev Service |
| UAT Environment | `http://uat-api.com` | ❌ No | Real UAT Service |
| Production | `http://api.prod.com` | ❌ No | Real Prod Service |
| No Config | "" (empty) | ❌ No | Nothing (tests fail) |
