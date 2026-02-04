#!/bin/bash

# Standalone Allure Report Generator
# Generates a self-contained report that can be shared with business/PO
# The report can be opened directly in a browser without a server

set -e

echo "=========================================="
echo "Generating Standalone Allure Report"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Run tests if needed
if [ ! -d "target/allure-results" ] || [ -z "$(ls -A target/allure-results)" ]; then
    echo -e "${BLUE}Running tests to collect results...${NC}"
    mvn clean test -DbaseUrl=http://localhost:9090
fi

# Generate report
echo -e "${BLUE}Generating Allure report...${NC}"
mvn allure:report

# Create standalone directory
STANDALONE_DIR="target/allure-standalone-report"
REPORT_DIR="target/allure-report"

if [ -d "$REPORT_DIR" ]; then
    echo -e "${BLUE}Creating standalone report package...${NC}"
    
    # Create standalone directory
    mkdir -p "$STANDALONE_DIR"
    
    # Copy report files
    cp -r "$REPORT_DIR"/* "$STANDALONE_DIR/"
    
    # Create a README for the report
    cat > "$STANDALONE_DIR/README.txt" << EOF
========================================
Allure Test Report
========================================

This is a standalone Allure test report that can be opened directly in any web browser.

HOW TO VIEW:
1. Open the 'index.html' file in your web browser
2. No web server required - works offline
3. All assets are included in this directory

REPORT INFORMATION:
- Generated: $(date)
- Project: Person Address Service API Testing
- Test Framework: JUnit 5 with Pact and WireMock

REPORT CONTENTS:
- Test execution results
- Test case details
- Pass/fail statistics
- Test duration and timing
- Error details and stack traces

For questions, contact the QA Team.
========================================
EOF

    echo ""
    echo -e "${GREEN}✓ Standalone report generated successfully!${NC}"
    echo ""
    echo -e "${YELLOW}Standalone report location:${NC}"
    echo "  Directory: $(pwd)/$STANDALONE_DIR"
    echo "  Main file: $(pwd)/$STANDALONE_DIR/index.html"
    echo ""
    echo -e "${YELLOW}To share with business/PO:${NC}"
    echo "  1. Zip the directory: zip -r allure-report.zip $STANDALONE_DIR"
    echo "  2. Share the zip file"
    echo "  3. Recipients can extract and open index.html in any browser"
    echo ""
    echo -e "${GREEN}=========================================="
    echo "Report ready for distribution!"
    echo "==========================================${NC}"
else
    echo -e "${YELLOW}Error: Report directory not found${NC}"
    exit 1
fi


