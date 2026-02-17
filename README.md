# mataku.com

Personal blog built with Kotlin.

## Modules

### Generator (Kotlin/JVM)

Converts Markdown files with YAML frontmatter to HTML. Uses `org.commonmark:commonmark` for GFM parsing.

### Worker (Kotlin/Wasm)

Cloudflare Worker implemented in Kotlin/Wasm. Serves content from Worker Assets. This module serves as a technical experiment to explore Kotlin/Wasm capabilities.

## Build Commands

```shell
make new awesome-article   # Generate articles/awesome-article.md
make generate              # Generate HTML from markdown
make build-worker          # Build Cloudflare Worker
```
