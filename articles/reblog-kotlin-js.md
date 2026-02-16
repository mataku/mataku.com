---
title: ブログを作り直した
date: 2026-02-16T12:24:42+09:00
tags:
  - Kotlin
  - Cloudflare Workers
---

Hugo と素敵なテーマで構成していたページを、Claude Code と慣れ親しんだ Kotlin を利用して自前運用するようにした。コントロールできる範囲を増やしたかったのと、コーディングエージェントがあれば自前運用の負荷も下がるだろうという期待からの判断。


## Markdown から記事の HTML 生成

Markdown をベースにして HTML へ変換する構成は変えたくなかったので、commonmark-java を利用し自分なりの GitHub Flavored Markdown ベースの SSG を作ることにした。

記事生成は GitHub Actions 上や手元のマシンでしか動かさないので JVM のライブラリを利用できる。Kotlin multiplatform のことばかり考えていたので途中 JetBrains/markdown も試したが、今回はパフォーマンスの良い commonmark-java を採用した。

https://github.com/mataku/mataku.com/tree/7b12cc631f45a38fc479a24cc6b1abf097135ab2/generator

## ページを serve する機構

省力化しつつも新しい刺激がある構成を目指し、Cloudflare Workers の Fetch Handler を Kotlin/JS で生成し、path に応じて Workers の assets 機能を使ってコンテンツを返すようにした。

Cloudflare 公式の https://github.com/cloudflare/kotlin-worker-hello-world を渡して Claude Code に作ってもらった。若干古いので multiplatform plugin を適用するマイグレーションさえすれば問題なし。

https://github.com/mataku/mataku.com/blob/7b12cc631f45a38fc479a24cc6b1abf097135ab2/worker/src/jsMain/kotlin/worker.kt


### HTML handling

https://developers.cloudflare.com/workers/static-assets/routing/advanced/html-handling/#automatic-trailing-slashes-default


デフォルトの HTML handling 設定は `auto-trailing-slash` なので、path が `articles/hoge` でも `articles/hoge.html` でも良いように柔軟な matcher が働いてくれる。


```json5
// wrangler.jsonc
{
  "name": "mataku-com",
  "main": "worker/entry.js",
  "compatibility_date": "2026-02-05",
  "assets": {
    "directory": "./output/",
    "run_worker_first": false,
    "binding": "ASSETS"
  },
  "observability": {
    "enabled": true
  }
}
```

assets.run_worker_first を true にすると、全部 main で指定した handler で受ける。false にするとまずデフォルトの static asset serving により対象の asset を探し、なければ main で指定した handler で fallback される。

全コンテンツ受けられるように書いたものの、特に認証機構もないし余計に Worker script が発火されなければリクエスト数も増えないので、一旦デフォルトの false で運用し始めた。


### Pages

元々使っていた Pages でも今は問題ない。ただ最近は Workers の方に注力しているようで migration 用のページも用意されていたり、ダッシュボード内の Workers & Pages から作成する際に、Pages への導線がささやかなものになっている。

Pages 利用のアプリケーションでも Workers にて利用できるように機能拡充をしているようで、Static Assets を利用すれば worker script へのアクセス数のみが課金対象になる。無料枠もある程度あるため特に問題なく移行できた。

https://developers.cloudflare.com/workers/static-assets/migration-guides/migrate-from-pages/

## デザイン

Claude Code plugin の frontend-design を使ってひたすらやりとりした。  
https://github.com/anthropics/claude-code/blob/8c09097e8c2565c4c9c107cb9ad1cfcb87366368/plugins/frontend-design/skills/frontend-design/SKILL.md

フロントエンドを全くと言っていいほどやってこなかったため、あいまいさを言語化するのが本当に難しかった。モバイルアプリ開発で馴染みのある UI パターンを手がかりに、具体的なポイントをいくつか伝えるようにした。配色は https://materialui.co/colors や https://www.radix-ui.com/colors を参考に key colors を選び、accent color には Kotlin のメインの色を指定して進めた。

## おわりに

カバーする範囲こそ多くなったもののコントロールできる範囲が増えたことで、結果的に負荷は下がり Kotlin になったことで変更しやすくもなったため良かった。Claude Code に書いてもらうなら Kotlin である理由も薄まりそうではある。

フロントエンドの改修をする際には気付けばずっと開発し続けるため、 Claude Code の rate limit が手を止める気分転換になって良かった。