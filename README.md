# mataku.com

Personal blog built with Kotlin.

## Modules

### Generator (Kotlin/JVM)

Converts Markdown files with YAML frontmatter to HTML. Uses `org.jetbrains:markdown` for GFM parsing.

### Worker (Kotlin/JS)

Cloudflare Workers fetch handler compiled from Kotlin/JS. Serves static files from R2 bucket.

## Build Commands

```bash
make generate      # Generate HTML from markdown
make build-worker  # Compile Kotlin/JS for Workers
```
