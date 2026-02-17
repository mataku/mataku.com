# Kotlin/Wasm TODO

Kotlin/Wasm (Cloudflare Worker) to leverage ideas.

## 1. OGP Image Generation

Generate OGP images dynamically in the Worker based on article title and date.

- Build SVG template with article metadata embedded, convert to PNG via Resvg Wasm build
- Add route like `/articles/og/<slug>.png`
- Cache generated images with Cloudflare Cache API to avoid regeneration
- Wasm advantage: image rendering is CPU-bound processing

### Challenges

- Limited image generation libraries available for Kotlin/Wasm
- Need to bridge Resvg (Rust/Wasm) via JS interop

## 2. Full-Text Search API

Provide a search endpoint that queries a pre-built index at runtime in the Worker.

- Generate an inverted index at build time (`make generate`) and include it in Static Assets
- Worker loads the index and performs search/scoring on request
- Add endpoint like `/api/search?q=<query>`
- Wasm advantage: index traversal and scoring are CPU-bound processing

### Approach

- Generator (JVM) builds the inverted index from article content and outputs as JSON
- Worker fetches the index from Static Assets on first request and caches it
- Share tokenization/indexing logic via `commonMain` (Kotlin Multiplatform)

## 3. Markdown Server-Side Rendering

Render Markdown to HTML at request time in the Worker instead of at build time.

- Store raw Markdown sources in Static Assets
- Worker converts Markdown to HTML on request
- Share Markdown parser logic between generator (JVM) and worker (Wasm) via `commonMain`
- Wasm advantage: Kotlin Multiplatform code sharing showcase

### Considerations

- Currently generator uses `org.commonmark` (JVM library), would need a pure Kotlin parser for `commonMain`
- Adds latency compared to serving pre-built HTML

## 4. Dynamic Image Resize

Resize images on the fly based on query parameters.

- Handle requests like `/images/photo.png?w=300`
- Decode, resize, and re-encode images in the Worker
- Wasm advantage: image processing is a typical Wasm-suitable workload

### Considerations

- Cloudflare already offers Image Resizing service, so this is mainly for learning purposes
- High implementation complexity

## 5. Dynamic RSS/Atom Feed Generation

Generate `feed.xml` dynamically from `articles.json` at request time.

- Worker fetches `articles.json` from Static Assets and builds XML response
- Enables future features like tag-based feeds or pagination
- Wasm advantage: sorting/filtering with large article counts

### Considerations

- Lower Wasm utilization compared to other ideas
- Low implementation complexity, good starting point

## Priority

| Idea | Wasm Utilization | Practicality | Complexity |
|---|---|---|---|
| OGP Image Generation | High | High | High |
| Full-Text Search API | High | High | Medium |
| Markdown SSR | Medium | Medium | Medium |
| Dynamic Image Resize | High | Low | High |
| Dynamic Feed Generation | Low-Medium | Medium | Low |
