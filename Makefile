.PHONY: test lint coverage coverage-rust coverage-frontend coverage-summary clean

test: test-rust test-frontend

test-rust:
	cargo fmt --check
	cargo clippy -- -D warnings
	cargo test

test-frontend:
	cd ui && npm run lint
	cd ui && npx tsc --noEmit
	cd ui && npm test

lint:
	cargo fmt --check
	cargo clippy -- -D warnings
	cd ui && npm run lint

coverage: coverage-rust coverage-frontend coverage-summary

coverage-rust:
	@echo "=== Rust coverage (cargo-tarpaulin) ==="
	cargo tarpaulin --workspace --out xml --out html --output-dir target/coverage --skip-clean

coverage-frontend:
	@echo "=== Frontend coverage (vitest + v8) ==="
	cd ui && npm run test:coverage

coverage-summary:
	@echo ""
	@echo "=== Coverage Summary ==="
	@echo "Rust:     target/coverage/tarpaulin-report.html"
	@echo "Frontend: ui/coverage/index.html"
	@echo ""
	@if [ -f cobertura.xml ]; then \
		echo "Rust line coverage:"; \
		tail -n 5 cobertura.xml 2>/dev/null || true; \
	fi
	@echo ""
	@echo "Open the HTML reports in a browser for file-level details."

clean:
	cargo clean
	rm -rf ui/coverage target/coverage cobertura.xml
