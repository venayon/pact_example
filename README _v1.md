# WireMock Stub Files

This directory contains externalized JSON stub files for the Citizen Address Service API testing.

## Directory Structure

```
test/resources/stubs/
├── get/                                    # GET endpoint stubs
│   ├── get-address-success-CID123456.json
│   ├── get-address-notfound-CID999999.json
│   ├── get-address-servererror-CID777777.json
│   ├── get-address-downingstreet-CID200001.json
│   ├── get-address-edinburghcastle-CID200002.json
│   ├── get-address-cardiffcityhall-CID200003.json
│   └── get-address-belfastcityhall-CID200004.json
└── post/                                   # POST endpoint stubs
    ├── post-address-success-request.json
    ├── post-address-success-response.json
    ├── post-address-badrequest-request.json
    ├── post-address-badrequest-response.json
    ├── post-address-notfound-request.json
    ├── post-address-notfound-response.json
    ├── post-address-servererror-request.json
    └── post-address-servererror-response.json
```

## Stub Files Organization

### GET Endpoint Stubs

All GET endpoint response files are located in `get/` directory.

| File | Citizen ID | Status | Description |
|------|-----------|--------|-------------|
| `get-address-success-CID123456.json` | CID123456 | 200 | Successful retrieval with Flat 5B, Westminster |
| `get-address-notfound-CID999999.json` | CID999999 | 404 | Non-existent citizen error |
| `get-address-servererror-CID777777.json` | CID777777 | 500 | Internal server error |
| `get-address-downingstreet-CID200001.json` | CID200001 | 200 | 10 Downing Street, London |
| `get-address-edinburghcastle-CID200002.json` | CID200002 | 200 | Edinburgh Castle, Scotland |
| `get-address-cardiffcityhall-CID200003.json` | CID200003 | 200 | Cardiff City Hall, Wales |
| `get-address-belfastcityhall-CID200004.json` | CID200004 | 200 | Belfast City Hall, Northern Ireland |
| `get-address-legacy5lines-CID300001.json` | CID300001 | 200 | **Legacy UK 5-line format** (Apartment 12, The Royal Chambers, 45 Victoria Street, Westminster, London) |

### POST Endpoint Stubs

POST endpoint files are paired (request + response) and located in `post/` directory.

| Request File | Response File | Citizen ID | Status | Description |
|--------------|---------------|-----------|--------|-------------|
| `post-address-success-request.json` | `post-address-success-response.json` | CID123456 | 201 | Create 221B Baker Street |
| `post-address-badrequest-request.json` | `post-address-badrequest-response.json` | CID400BAD | 400 | Missing postcode validation |
| `post-address-notfound-request.json` | `post-address-notfound-response.json` | CID888888 | 404 | Non-existent citizen |
| `post-address-servererror-request.json` | `post-address-servererror-response.json` | CID555555 | 500 | Database write failure |
| `post-address-legacy5lines-request.json` | `post-address-legacy5lines-response.json` | CID300002 | 201 | **Legacy UK 5-line format** (Flat 7C, Windsor Court, 123 Kensington Road, Kensington, London) |

## Usage in WireMockService

The `WireMockService.java` reads these files using the `readStubFile()` helper method:

```java
private static String readStubFile(String filePath) {
    try {
        return Files.readString(Paths.get(STUBS_BASE_PATH, filePath));
    } catch (IOException e) {
        throw new RuntimeException("Failed to read stub file: " + filePath, e);
    }
}
```

Example usage:
```java
wireMockServer.stubFor(get(urlEqualTo("/citizen/CID123456/address"))
    .withHeader("Subsystem-Id", equalTo("CONSUMER_SYSTEM_001"))
    .withHeader("Correlation-Id", matching(".*"))
    .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody(readStubFile("get/get-address-success-CID123456.json"))
    ));
```

## Benefits of Externalized Stubs

1. **Maintainability**: Easy to update response data without changing Java code
2. **Readability**: JSON files are easier to read and edit than inline strings
3. **Reusability**: Same stub files can be used across multiple test scenarios
4. **Version Control**: Better diff visualization in Git for JSON changes
5. **Team Collaboration**: Non-developers can update test data easily
6. **IDE Support**: Syntax highlighting and validation for JSON files

## Adding New Stubs

To add a new stub:

1. Create the JSON file in appropriate directory (`get/` or `post/`)
2. Follow naming convention: `{method}-address-{scenario}-{citizenId}.json`
3. Update `WireMockService.java` to reference the new file
4. Document the stub in this README

## Naming Convention

**GET Responses:**
```
get-address-{scenario}-{citizenId}.json
```

**POST Request/Response Pairs:**
```
post-address-{scenario}-request.json
post-address-{scenario}-response.json
```

Where `{scenario}` can be:
- `success`
- `badrequest`
- `notfound`
- `servererror`
- Custom descriptive names (e.g., `downingstreet`, `edinburghcastle`)

## UK Address Examples

All stub files contain realistic UK addresses with valid postcodes:

- **SW1A 1AA** - Westminster Palace area, London
- **NW1 6XE** - Baker Street area, London
- **SW1A 2AA** - 10 Downing Street, London
- **EH1 2NG** - Edinburgh Castle, Scotland
- **CF10 3ND** - Cardiff City Hall, Wales
- **BT1 5GS** - Belfast City Hall, Northern Ireland

### Legacy UK Address Format (5 Address Lines)

The system supports legacy UK address format with 5 structured address lines:

**Format:**
1. **Line 1**: Apartment/Flat number (e.g., "Apartment 12", "Flat 7C")
2. **Line 2**: Building name (e.g., "The Royal Chambers", "Windsor Court")
3. **Line 3**: Street number and name (e.g., "45 Victoria Street", "123 Kensington Road")
4. **Line 4**: District/Area (e.g., "Westminster", "Kensington")
5. **Line 5**: City/Town (e.g., "London")

**Examples in Stubs:**
- **CID300001** (GET): Apartment 12, The Royal Chambers, 45 Victoria Street, Westminster, London (SW1H 0NW)
- **CID300002** (POST): Flat 7C, Windsor Court, 123 Kensington Road, Kensington, London (W8 5SA)

This format is commonly used in legacy UK systems and government databases where address components are stored in separate fields.
