.PHONY: generate dev deploy new feed build-worker serve

generate:
	./gradlew :generator:run --no-daemon

dev:
	DEV=1 ./gradlew :generator:run -q

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

serve:
	npx wrangler dev

deploy: build-worker generate
	npx wrangler deploy
