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
