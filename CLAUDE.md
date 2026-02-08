# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Generate article HTML + articles.json + index.html → output/
make generate
# or: ./gradlew :generator:run

# Create a new article markdown file
make new my-article
# or: ./gradlew :generator:new --args="my-article"

# Compile JS for Cloudflare Workers
make build-worker
# or: ./gradlew :worker:compileProductionExecutableKotlinJs
```

## Architecture

Gradle multi-module project (Kotlin 2.1.0) consisting of two modules:

### Generator Module (Kotlin/JVM) — Static Site Generator

Converts `articles/*.md` (with YAML frontmatter) to HTML and outputs them to `output/`.

**Pipeline:** `FrontmatterParser.parse()` → convert markdown body to HTML with `org.jetbrains:markdown` → replace `{{key}}` placeholders with `TemplateEngine.render()` → write to `output/`

- `Generator`: Generates HTML for all articles, produces `articles.json` (metadata JSON), and copies `templates/index.html` to output
- `ArticleCreator`: Creates new article markdown files from `templates/article.md` with current date
- `TemplateEngine`: Regex-based `{{key}}` placeholder replacement
- `FrontmatterParser`: Parses `---`-delimited YAML frontmatter with SnakeYAML Engine, returns `Article(metadata, content)`
- `JsonWriter`: Manually builds JSON strings without external libraries (RFC 8259 escape handling)

### Worker Module (Kotlin/JS) — Cloudflare Workers

A Workers fetch handler that serves static files from an R2 bucket. Compiled to JS with the Kotlin/JS IR compiler and deployed to Workers via `worker/entry.js` (ESM wrapper).

- `worker.kt`: `@JsExport fun fetch(request, env)` → fetches files from R2 via `env.BUCKET.get(key)`
- `worker/entry.js`: Wrapper that converts Kotlin/JS CommonJS output to ES Module `export default`
- `wrangler.toml`: Workers + R2 bucket configuration

## Key Directories

- `articles/` — Markdown article sources (frontmatter: title, date)
- `templates/` — Templates (`article.html`, `index.html`, `article.md` for new articles)
- `output/` — Generated output (HTML, CSS, articles.json). `styles.css` is manually maintained
