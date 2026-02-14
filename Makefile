.PHONY: generate deploy new feed build-worker

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

build-worker:
	./gradlew :worker:compileProductionExecutableKotlinJs --no-daemon

feed:
	./gradlew :generator:feed --no-daemon

deploy: build-worker generate
	npx wrangler deploy
