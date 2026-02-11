.PHONY: generate deploy new build-worker feed deploy_images deploy_aseets_local deploy_images_local

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

build-worker:
	./gradlew :worker:compileProductionExecutableKotlinJs --no-daemon

deploy_assets: build-worker
	npx wrangler r2 object put mataku-blog/index.html --file=output/index.html --remote
	# npx wrangler r2 object put mataku-blog/404.html --file=output/404.html --remote
	@for file in output/assets/*; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/assets/$$filename --file=$$file --remote; \
	done
	npx wrangler deploy

deploy_assets_local: build-worker
	npx wrangler r2 object put mataku-blog/index.html --file=output/index.html --local
	npx wrangler r2 object put mataku-blog/404.html --file=output/404.html --local
	@for file in output/assets/*; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/assets/$$filename --file=$$file --local; \
	done

deploy_images:
	@for file in output/images/*; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/images/$$filename --file=$$file --remote; \
	done

deploy_images_local:
	@for file in output/images/*; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/images/$$filename --file=$$file --local; \
	done

deploy: generate build-worker
	@for file in output/articles/*.html; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/articles/$$filename --file=$$file --remote; \
	done
	npx wrangler r2 object put mataku-blog/articles.json --file=output/articles.json --remote
	npx wrangler r2 object put mataku-blog/feed.xml --file=output/feed.xml --remote
	npx wrangler r2 object put mataku-blog/index.html --file=output/index.html --remote
	npx wrangler r2 object put mataku-blog/404.html --file=output/404.html --remote
	npx wrangler r2 object put mataku-blog/assets/styles.css --file=output/assets/styles.css --remote
	# npx wrangler deploy

deploy_local: generate build-worker
	@for file in output/articles/*.html; do \
		filename=$$(basename $$file); \
		npx wrangler r2 object put mataku-blog/articles/$$filename --file=$$file --local; \
	done
	npx wrangler r2 object put mataku-blog/articles.json --file=output/articles.json --local
	npx wrangler r2 object put mataku-blog/feed.xml --file=output/feed.xml --local
	npx wrangler r2 object put mataku-blog/index.html --file=output/index.html --local
	npx wrangler r2 object put mataku-blog/404.html --file=output/404.html --local
