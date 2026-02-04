# Allure Test Reporting Guide

This guide explains how to generate and view Allure test reports for business stakeholders and Product Owners.

## Overview

Allure is a flexible, lightweight multi-language test report tool that provides clear, visual representation of test execution results. The reports are designed to be easily understood by both technical and non-technical stakeholders.

## Quick Start

### Generate and View Report

1. **Run tests and generate report:**
   ```bash
   ./generate-allure-report.sh
   ```

2. **View report in browser:**
   ```bash
   ./serve-allure-report.sh
   ```
   This will start a local web server and open the report in your default browser.

### Generate Standalone Report (for sharing)

To create a report that can be shared with business/PO:

```bash
./generate-standalone-report.sh
```

This creates a self-contained report in `target/allure-standalone-report/` that can be:
- Zipped and emailed
- Shared via file sharing services
- Opened directly in any browser (no server needed)

## Manual Commands

### Run Tests and Collect Results

```bash
mvn clean test -DbaseUrl=http://localhost:9090
```

### Generate Report

```bash
mvn allure:report
```

Report will be generated in: `target/allure-report/index.html`

### Serve Report Locally

```bash
mvn allure:serve
```

This starts a local web server (usually on http://localhost:XXXX) and opens the report in your browser.

## Report Features

### For Business Stakeholders

The Allure report provides:

1. **Executive Summary**
   - Total tests executed
   - Pass/fail statistics
   - Overall test duration
   - Success rate percentage

2. **Test Results Overview**
   - Visual charts and graphs
   - Test execution timeline
   - Status breakdown (passed, failed, skipped)

3. **Detailed Test Information**
   - Test case descriptions (business-friendly language)
   - Test steps and actions
   - Expected vs actual results
   - Screenshots and attachments (if added)

4. **Filtering and Search**
   - Filter by test status
   - Search by test name
   - Filter by features/epics/stories
   - Filter by severity

### Report Sections

- **Overview**: High-level statistics and charts
- **Behaviors**: Tests organized by features and stories
- **Packages**: Tests organized by package/class
- **Suites**: Tests organized by test suite
- **Graphs**: Visual representations of test results
- **Timeline**: Test execution timeline
- **Retries**: Information about retried tests

## Test Annotations Used

Our tests are annotated with Allure annotations to provide rich reporting:

- `@Epic`: High-level feature grouping (e.g., "Person Address Service")
- `@Feature`: Feature-level grouping (e.g., "API Contract Testing")
- `@Story`: User story or scenario (e.g., "Get Person by ID")
- `@DisplayName`: Human-readable test name
- `@Description`: Detailed test description
- `@Severity`: Test priority (BLOCKER, CRITICAL, NORMAL, MINOR, TRIVIAL)
- `@Owner`: Test owner/team

## Sharing Reports with Business/PO

### Option 1: Standalone Report (Recommended)

1. Generate standalone report:
   ```bash
   ./generate-standalone-report.sh
   ```

2. Create zip file:
   ```bash
   zip -r allure-report-$(date +%Y%m%d).zip target/allure-standalone-report
   ```

3. Share the zip file via email or file sharing service

4. Recipients extract and open `index.html` in any browser

### Option 2: Hosted Report

If you have a web server available:

1. Upload the `target/allure-report` directory to your web server
2. Share the URL with stakeholders
3. They can access it from anywhere

### Option 3: Screenshots/PDF

For quick sharing:

1. Open the report in browser
2. Take screenshots of key sections
3. Or use browser's print-to-PDF feature

## Report Interpretation

### Understanding Test Status

- **Passed (Green)**: Test executed successfully
- **Failed (Red)**: Test encountered an error or assertion failure
- **Skipped (Yellow)**: Test was not executed (e.g., conditional skip)
- **Broken (Orange)**: Test failed due to infrastructure/configuration issues

### Key Metrics

- **Total Tests**: Number of test cases executed
- **Pass Rate**: Percentage of tests that passed
- **Duration**: Total time taken to execute all tests
- **Flakiness**: Tests that sometimes pass and sometimes fail

## Troubleshooting

### No Results Generated

If you see "No test results found":

1. Make sure tests have been executed:
   ```bash
   mvn clean test
   ```

2. Check that `target/allure-results` directory exists and contains files

### Report Not Opening

1. Make sure you have generated the report:
   ```bash
   mvn allure:report
   ```

2. Check that `target/allure-report/index.html` exists

3. Try opening the HTML file directly in your browser

### Port Already in Use

If `mvn allure:serve` fails due to port conflict:

1. Kill the process using the port
2. Or use a different port (configure in pom.xml)

## Best Practices

1. **Regular Reporting**: Generate reports after each test run
2. **Version Control**: Include test run date/time in report names
3. **Documentation**: Add descriptions to tests for business clarity
4. **Categorization**: Use Epics, Features, and Stories to organize tests
5. **Severity**: Mark critical tests with appropriate severity levels

## Additional Resources

- [Allure Documentation](https://docs.qameta.io/allure/)
- [Allure Best Practices](https://docs.qameta.io/allure/#_best_practices)

## Support

For questions or issues with test reporting, contact the QA Team.


