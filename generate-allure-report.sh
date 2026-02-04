#!/bin/bash

# Allure Report Generation Script
# This script generates Allure reports for business stakeholders and Product Owners

set -e

echo "=========================================="
echo "Allure Report Generation"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Run tests and generate Allure results
echo -e "${BLUE}Step 1: Running tests and collecting Allure results...${NC}"
mvn clean test -DbaseUrl=http://localhost:9090

# Generate Allure report
echo ""
echo -e "${BLUE}Step 2: Generating Allure report...${NC}"
mvn allure:report

# Get the report path
REPORT_DIR="target/allure-report"
REPORT_INDEX="$REPORT_DIR/index.html"

if [ -f "$REPORT_INDEX" ]; then
    echo ""
    echo -e "${GREEN}✓ Allure report generated successfully!${NC}"
    echo ""
    echo -e "${YELLOW}Report location:${NC}"
    echo "  File: $(pwd)/$REPORT_INDEX"
    echo ""
    echo -e "${YELLOW}To view the report:${NC}"
    echo "  1. Open in browser: file://$(pwd)/$REPORT_INDEX"
    echo "  2. Or serve locally: mvn allure:serve"
    echo ""
    echo -e "${GREEN}=========================================="
    echo "Report ready for business/PO review!"
    echo "==========================================${NC}"
else
    echo -e "${YELLOW}Warning: Report index file not found at $REPORT_INDEX${NC}"
    exit 1
fi


