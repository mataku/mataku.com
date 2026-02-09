.PHONY: generate deploy new build-worker feed deploy_images

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

feed:
	./gradlew :generator:feed

build-worker:
	./gradlew :worker:compileProductionExecutableKotlinJs

deploy_assets: build-worker
	npx wrangler r2 object put mataku-blog/index.html --file=output/index.html --remote
	# npx wrangler r2 object put mataku-blog/404.html --file=output/404.html --remote
	@for file in output/assets/*; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/assets/$$filename --file=$$file --remote; \
	done
	npx wrangler deploy

deploy_images:
	@for file in output/images/*; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/images/$$filename --file=$$file --remote; \
	done

deploy: generate build-worker
	@for file in output/articles/*.html; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/articles/$$filename --file=$$file --remote; \
	done
	npx wrangler r2 object put mataku-blog/articles.json --file=output/articles.json --remote
	npx wrangler r2 object put mataku-blog/feed.xml --file=output/feed.xml --remote
	# npx wrangler deploy