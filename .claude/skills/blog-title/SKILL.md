---
name: blog-title
description: Generate blog title file name
context: fork
disable-model-invocation: true
---

## Blog title

Generate a blog article filename from the given argument.

### Argument validation

**If no argument is provided, output the following error message and stop:**

```
Error: Title argument is required.
Usage: /blog-title <title>
Example: /blog-title FlutterでWebViewを使う方法
```

### Rules

1. **English only**: Translate to English if Japanese is provided
2. **Lowercase**: All characters must be lowercase
3. **Hyphen-separated**: Use hyphens (`-`) as word separators
4. **Concise**: Keep it to 3-5 words. Remove verbose expressions
5. **No extension**: Do not include `.md`

### Output format

Output only the filename. No explanation needed.

### Examples

- Input: `FlutterでWebViewを使う方法`
  Output: `flutter-webview-usage`

- Input: `Android の Jetpack Compose 入門`
  Output: `jetpack-compose-intro`

- Input: `How to setup GitHub Actions`
  Output: `github-actions-setup`
