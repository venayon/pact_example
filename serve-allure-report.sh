#!/bin/bash

# Allure Report Server Script
# This script serves the Allure report on a local web server for easy viewing

set -e

echo "=========================================="
echo "Starting Allure Report Server"
echo "=========================================="
echo ""

# Check if results exist
if [ ! -d "target/allure-results" ] || [ -z "$(ls -A target/allure-results)" ]; then
    echo "No Allure results found. Running tests first..."
    mvn clean test -DbaseUrl=http://localhost:9090
fi

# Serve the report
echo "Starting Allure server..."
echo "The report will open in your default browser."
echo "Press Ctrl+C to stop the server."
echo ""

mvn allure:serve


