---
title: minne と Kotlin
date: 2019-01-09T22:18:01+09:00
draft: false
tags:
  - Android
---

original post: https://matakucom.medium.com

- - - 

ちょっと前に minne Android アプリに Kotlin を導入しました。正確にはテストコードとしてはその前から使われていて、アプリケーションコードに導入した形になります。導入段階のためそこまでこれ辛かったよねみたいな話は出てませんが、その際にどんなことをしたのかを書きます。


## Kotlin のコードに触れる

テストコードである程度文法は理解しているのと、そもそも他のメンバーがある程度 kotlin について理解があったため、アプリケーションコードとしてどう書いていくか、Java とどういった差分になるかを理解してもらえれば導入としては充分でした。 UI 層、Model 層それぞれで変換事例を作っています。(minne では MVP アーキテクチャを採用しています)

将来的に kotlin 化を考えている大きめなプロジェクトがテストコードから kotlin で書き始めるというのは、アプリケーションへの影響範囲がテスト内容のみで済むためとても有効だと思います。ドキュメントを見たり、雑でも良いので kotlin + プロダクトで使っているライブラリを用いてアプリを作ってみるというのは現実的ですね。

## Kapt (annotation processing 周り)

Kotlin によるクラス内でもアノテーションプロセッサによるコード生成を行いたかったためライブラリ指定を annotationProcessor から kapt に差し替えています。その際、lombok によるクラスのプロパティにアクセスできずにビルドが通らない問題がありました。以下のような感じです。

https://discuss.kotlinlang.org/t/kotlin-java-lombok-interop/1442

これに対する対応方針として以下の 2 つを考えました。

1. Delombok して lombok の依存を外す
2. Lombok を使用しているクラスを別モジュールに切り出しビルドできるようにする


minne では delombok して依存を外す で対応しています。というのも model 層 (entity) 内にて Getter, Setter の生成として使用しているのがほとんどなため、kotlin の data class でそこまで負荷なく移行できると考えたためです。

2 つ目の選択肢を考えたときに、minne では機能という関心をもってモジュール分割する方針を取ろうとしている背景がありました。それを踏まえると関心事が複雑すぎるなということで見送りました。

kapt に関しては、アノテーションプロセッサによって生成される型を参照する部分があり、 NonExistentClass のままだと困るため `kapt.correctErrorTypes = true` を指定しています。


## コードスタイル

チームみんな Android Studio を使って開発しているため、 Intellij Idea の設定ファイルを用いています。公式が紹介しているスタイルを設定できるためそれと、最低限必要な空行の設定で今は収まっています。最初からがちがちにせずレビュー時に困ったら追加するようにしています。

```xml
<!-- .idea/codeStyles/Project.xml -->
<codeStyleSettings language="kotlin">
  <option name="CODE_STYLE_DEFAULTS" value="KOTLIN_OFFICIAL" />
  <option name="KEEP_BLANK_LINES_IN_DECLARATIONS" value="1" />
  <option name="KEEP_BLANK_LINES_IN_CODE" value="1" />
  <option name="KEEP_BLANK_LINES_BEFORE_RBRACE" value="0" />
</codeStyleSettings>
```

Ktlint を CI のワークフロー に組み込んで実行させていただき、この辺を機械的に担保しています。

## 変換方針

進めている施策で関連するクラスが Java であれば一緒に置換してリファクタリングする方針です。

一括で集中して置換したかったのですがファイル数が多く、他とコンフリクトする恐れがある + レビュー負荷を踏まえ、他の施策を止めてまでやるかどうかというところをもって上記の判断をしました。Android Studio による自動変換機能は便利ですね。

```bash
# minne Android のアプリケーション層のファイル数 ( = おおまかなクラス数 )
$ find app/src/main/java/ -type f | wc -l
    1036
```

その他以下のような当たり前のものも含むルールが別途ありますが、この辺は別の形で紹介できればと思います。

- 名前空間の識別を明確にしたいのでスコープ関数は let または also を使い、apply は使わない
- Safe call, nullability を考慮した呼び出し
- Entity のプロパティを安易に Nullable にせず、NonNull なものは NonNull にする (そもそも Web API レスポンスをパースする用であれば自動生成する方向で考えたい)

## 今後

モジュール化されていれば影響範囲を推測しやすく、こういった変化への対応も楽だっただろうなあと思うため、積極的にモジュール化を考えていこうと個人的に考えています。このあたりで一家言ある方はぜひ色々お話聞かせてください。

https://recruit.pepabo.com/environment/engineer/
