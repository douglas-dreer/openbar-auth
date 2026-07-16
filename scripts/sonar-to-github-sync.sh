#!/bin/bash

# ============================================
# Script: SonarQube → GitHub Issues Sync
# ============================================
# This script fetches issues from SonarQube
# and creates corresponding GitHub issues.
#
# Usage:
#   ./scripts/sonar-to-github-sync.sh
#
# Environment variables required:
#   SONAR_URL - SonarQube server URL
#   SONAR_TOKEN - SonarQube authentication token
#   SONAR_PROJECT_KEY - SonarQube project key
#   GITHUB_TOKEN - GitHub personal access token
#   GITHUB_REPO - GitHub repository (owner/repo)
# ============================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}SonarQube → GitHub Issues Sync${NC}"
echo -e "${GREEN}========================================${NC}"

# Validate environment variables
required_vars=("SONAR_URL" "SONAR_TOKEN" "SONAR_PROJECT_KEY" "GITHUB_TOKEN" "GITHUB_REPO")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo -e "${RED}Error: $var is not set${NC}"
        exit 1
    fi
done

# Fetch issues from SonarQube
echo -e "\n${YELLOW}Fetching issues from SonarQube...${NC}"

SONAR_ISSUES=$(curl -s -u "${SONAR_TOKEN}:" \
    "${SONAR_URL}/api/issues/search?componentKeys=${SONAR_PROJECT_KEY}&types=BUG,VULNERABILITY,CODE_SMELL&statuses=OPEN&ps=100")

TOTAL_ISSUES=$(echo "$SONAR_ISSUES" | jq -r '.total')
echo -e "Found ${TOTAL_ISSUES} issues in SonarQube"

# Process each issue
echo "$SONAR_ISSUES" | jq -c '.issues[]' | while read -r issue; do
    KEY=$(echo "$issue" | jq -r '.key')
    RULE=$(echo "$issue" | jq -r '.rule')
    SEVERITY=$(echo "$issue" | jq -r '.severity')
    MESSAGE=$(echo "$issue" | jq -r '.message')
    COMPONENT=$(echo "$issue" | jq -r '.component')
    LINE=$(echo "$issue" | jq -r '.line // "N/A"')
    
    # Get rule description
    RULE_DESC=$(curl -s -u "${SONAR_TOKEN}:" \
        "${SONAR_URL}/api/rules/show?key=${RULE}" | jq -r '.htmlDesc // "No description"')
    
    # Check if issue already exists in GitHub
    EXISTING=$(curl -s -H "Authorization: token ${GITHUB_TOKEN}" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/${GITHUB_REPO}/issues?labels=sonarqube,${KEY}" | jq -r '.length')
    
    if [ "$EXISTING" -gt 0 ]; then
        echo -e "${YELLOW}Issue ${KEY} already exists in GitHub, skipping...${NC}"
        continue
    fi
    
    # Create GitHub issue
    echo -e "${YELLOW}Creating GitHub issue for ${KEY}...${NC}"
    
    TITLE="[SonarQube] ${SEVERITY}: ${MESSAGE}"
    
    BODY="## SonarQube Issue

| Field | Value |
|-------|-------|
| **Key** | ${KEY} |
| **Rule** | ${RULE} |
| **Severity** | ${SEVERITY} |
| **Component** | ${COMPONENT} |
| **Line** | ${LINE} |

### Description

${RULE_DESC}

### Resolution

This issue was automatically synced from SonarQube. Please fix the code smell or vulnerability as described in the rule documentation.

---
*Automatically created by sonar-to-github-sync script*"

    curl -s -X POST \
        -H "Authorization: token ${GITHUB_TOKEN}" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/${GITHUB_REPO}/issues" \
        -d "{
            \"title\": \"${TITLE}\",
            \"body\": $(echo "$BODY" | jq -Rs .),
            \"labels\": [\"sonarqube\", \"${KEY}\", \"${SEVERITY,,}\"]
        }" | jq -r '.number' | xargs -I {} echo -e "${GREEN}Created GitHub issue #{}${NC}"
    
    # Small delay to avoid rate limiting
    sleep 0.5
done

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Sync completed!${NC}"
echo -e "${GREEN}========================================${NC}"
