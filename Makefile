.DEFAULT_GOAL := help

GRADLE = cd java && ./gradlew --no-daemon

.PHONY: help list clean setup test \
	java-compile java-test java-coverage java-lint java-bootjar java-bench \
	frontend-lint frontend-typecheck frontend-test frontend-coverage frontend-build frontend-audit \
	playwright docker-build docker-smoke trivy-scan-api trivy-scan-ui gitleaks java-audit \
	ci-java ci-frontend ci-playwright ci-docker ci-security \
	quality quality-full

##@ Discovery
help: ## Show available targets grouped by category
	@echo "Usage: make <target>"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"} \
		/^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } \
		/^[a-zA-Z_-]+:.*?## / { printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)
	@echo ""

list: help ## Alias for help

##@ Bootstrap
setup: ## Install/bootstrap all dependencies (safe to re-run)
	cd ui && npm ci
	$(GRADLE) help

# ---------------------------------------------------------------------------
# Leaf targets
# ---------------------------------------------------------------------------

##@ Java
java-compile: ## Compile all Java modules
	$(GRADLE) compileJava compileTestJava

java-test: ## Run all Java tests
	$(GRADLE) test

java-coverage: ## Run Java tests with Jacoco coverage (enforces per-module coverage gates)
	$(GRADLE) test jacocoTestReport jacocoTestCoverageVerification

java-lint: ## Static analysis of Java sources (Checkstyle)
	$(GRADLE) checkstyleMain checkstyleTest

java-bootjar: ## Build the fat JAR (arcogine.jar)
	$(GRADLE) :sim-cli:bootJar

java-bench: ## Run JMH microbenchmarks (sim-core; on-demand, not a CI gate)
	$(GRADLE) :sim-core:jmh

##@ Frontend
frontend-lint: ## Lint frontend code (ESLint)
	cd ui && npm run lint

frontend-typecheck: ## Type-check frontend (tsc --noEmit)
	cd ui && npx tsc --noEmit

frontend-test: ## Run frontend unit tests (Vitest)
	cd ui && npm test

frontend-coverage: ## Run frontend tests with coverage
	cd ui && npm run test:coverage

frontend-build: ## Production build of the frontend
	cd ui && npm run build

frontend-audit: ## Audit npm dependencies
	cd ui && npm audit --audit-level=high

##@ E2E
playwright: ## Run Playwright E2E tests (requires running API + UI)
	cd ui && npx playwright test

##@ Docker
docker-build: ## Build container images via Docker Compose
	docker compose build

docker-smoke: ## Full smoke cycle: build, start, health-check, tear down
	cp .env.example .env
	docker compose up -d --wait --wait-timeout 120
	curl -sf http://localhost:3000/api/health > /dev/null
	curl -sf http://localhost:$${UI_PORT:-5173}/health > /dev/null
	docker compose down

##@ Security
trivy-scan-api: ## Scan API Docker image with Trivy
	docker build -t arcogine-api:ci .
	trivy image --severity CRITICAL,HIGH --ignore-unfixed --exit-code 1 arcogine-api:ci

trivy-scan-ui: ## Scan UI Docker image with Trivy
	docker build -t arcogine-ui:ci ui
	trivy image --severity CRITICAL,HIGH --ignore-unfixed --exit-code 1 arcogine-ui:ci

gitleaks: ## Scan repo for secrets with Gitleaks
	gitleaks detect --source . --config .gitleaks.toml --verbose

java-audit: ## Scan Java dependencies for CVEs (CycloneDX SBOM + Trivy; blocking)
	$(GRADLE) cyclonedxBom
	trivy sbom --severity CRITICAL,HIGH --ignore-unfixed --exit-code 1 java/build/reports/cyclonedx/bom.json

# ---------------------------------------------------------------------------
# Composite targets — CI-oriented groupings
# ---------------------------------------------------------------------------

##@ CI composites
ci-java: java-compile java-lint java-test java-coverage ## All Java quality gates
ci-frontend: frontend-lint frontend-typecheck frontend-coverage frontend-build frontend-audit ## All frontend quality gates
ci-playwright: playwright ## Playwright E2E (bootstrap handled by CI workflow)
ci-docker: docker-build docker-smoke ## Docker build + smoke test
ci-security: java-audit frontend-audit trivy-scan-api trivy-scan-ui gitleaks ## All security scans

# ---------------------------------------------------------------------------
# Developer entrypoints
# ---------------------------------------------------------------------------

##@ Quality gates
test: java-test frontend-test ## Run Java + frontend unit test suites
quality: java-compile java-lint java-test java-coverage frontend-lint frontend-typecheck frontend-test frontend-coverage frontend-build ## Fast quality gates (no Docker/Playwright/security)
quality-full: quality playwright docker-build docker-smoke ci-security ## Full quality gates (everything)

##@ Utility
clean: ## Remove build artifacts and coverage reports
	$(GRADLE) clean
	rm -rf ui/coverage
