.PHONY: generate deploy new build-worker

generate:
	./gradlew :generator:run

new:
	@if [ -z "$(filter-out $@,$(MAKECMDGOALS))" ]; then \
		echo "Usage: make new <article-name>"; \
		echo "Example: make new my-new-article"; \
		exit 1; \
	fi
	./gradlew :generator:new --args="$(filter-out $@,$(MAKECMDGOALS))"

%:
	@:

build-worker:
	./gradlew :worker:compileProductionExecutableKotlinJs

deploy: generate build-worker
	npx wrangler r2 object put mataku-blog/index.html --file=output/index.html --remote
	npx wrangler r2 object put mataku-blog/styles.css --file=output/styles.css --remote
	npx wrangler r2 object put mataku-blog/articles.js --file=output/articles.js --remote
	@for file in output/articles/*.html; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/articles/$$filename --file=$$file --remote; \
	done
	npx wrangler deploy
