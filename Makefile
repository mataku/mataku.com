.PHONY: generate deploy new feed

generate:
	./gradlew :generator:run --no-daemon

new:
	@if [ -z "$(filter-out $@,$(MAKECMDGOALS))" ]; then \
		echo "Usage: make new <article-name>"; \
		echo "Example: make new my-new-article"; \
		exit 1; \
	fi
	./gradlew :generator:new --args="$(filter-out $@,$(MAKECMDGOALS))" --no-daemon

%:
	@:

feed:
	./gradlew :generator:feed --no-daemon

deploy: generate
	npx wrangler pages deploy
