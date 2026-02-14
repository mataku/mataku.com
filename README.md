# mataku.com

Personal blog built with Kotlin.

## Modules

### Generator (Kotlin/JVM)

Converts Markdown files with YAML frontmatter to HTML. Uses `org.commonmark:commonmark` for GFM parsing.

### Worker (Kotlin/JS)

Cloudflare Worker implemented in Kotlin/JS. Serves content from Worker Assets.

## Build Commands

```shell
make new awesome-article   # Generate articles/awesome-article.md
make generate              # Generate HTML from markdown
make build-worker          # Build Cloudflare Worker
```
