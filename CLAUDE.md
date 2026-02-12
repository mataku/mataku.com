# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Generate article HTML + articles.json + index.html → output/
make generate

# Create a new article markdown file
make new my-article

# Generate RSS feed (feed.xml)
make feed

# Deploy to Cloudflare Pages (runs generate first)
make deploy
```

## Architecture

Gradle project (Kotlin) with a single generator module, deployed to Cloudflare Pages.

### Generator Module (Kotlin/JVM) — Static Site Generator

Converts `articles/*.md` (with YAML frontmatter) to HTML and outputs them to `output/`.

**Pipeline:** `FrontmatterParser.parse()` → convert markdown body to HTML with `org.commonmark` → replace `{{key}}` placeholders with `TemplateEngine.render()` → write to `output/`

- `Generator`: Generates HTML for all articles, produces `articles.json` (metadata JSON), and copies `templates/index.html` to output
- `ArticleCreator`: Creates new article markdown files from `templates/article.md` with current date
- `FeedGenerator`: Generates RSS feed (feed.xml)
- `TemplateEngine`: Regex-based `{{key}}` placeholder replacement
- `FrontmatterParser`: Parses `---`-delimited YAML frontmatter with SnakeYAML Engine, returns `Article(metadata, content)`
- `JsonWriter`: Manually builds JSON strings without external libraries (RFC 8259 escape handling)

### Cloudflare Pages

Static files in `output/` are deployed to Cloudflare Pages via `wrangler pages deploy`.

- `wrangler.toml`: Pages project configuration (project name: `mataku-com`, output dir: `output/`)

## Key Directories

- `articles/` — Markdown article sources (frontmatter: title, date)
- `templates/` — Templates (`article.html`, `index.html`, `article.md` for new articles)
- `output/` — Generated output (HTML, CSS, articles.json, feed.xml). `styles.css` is manually maintained

## GitHub Actions Workflows

- `deploy.yaml` — Deploy to production on push to develop branch
- `preview.yaml` — Deploy preview environment on pull request
- `cleanup_preview.yaml` — Delete preview environment when PR is closed
- `build.yaml` — Build verification on push/PR to develop
