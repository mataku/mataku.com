---
title: webview_flutter_android の Payment Request API 対応
date: 2025-08-19T12:56:04+09:00
tags:
  - Flutter
  - Android
---

webview\_flutter\_android への修正がリリースされた。  
https://pub.dev/packages/webview_flutter_android/changelog#4100

## Context

Flutter をやることになったので、一通り基本的な機能があるようなアプリケーションを 1 つ真面目に作り、カジュアル面談みたいな場でフィードバックをもらった。その後 dart のコード読みたいなとなり flutter/packages を読んでいたら、ちょうど良さそうな issue: [[webview_flutter_android] Add support for Google Pay in WebView](https://github.com/flutter/flutter/issues/172150) がありちょうど良い題材かと至った。

## どう実装したか

WebView を通常利用する場合は webview\_flutter を使い、各プラットフォームの実装を意識することはないが、今回の issue は Android OS 特有の API 追加になるため、Android 実装である webview\_flutter\_android の修正になっている。そのため、[Contributing to webview_flutter_android](https://github.com/flutter/packages/blob/main/packages/webview_flutter/webview_flutter_android/CONTRIBUTING.md) に沿って以下のように作業した。

1. `pigeons/android_webkit.dart` に利用したいメソッドを作成。WebSettingsCompat や WebViewFeature を使った実装はまだなかったため、クラスごと新規作成
1. `dart run pigeon --input pigeons/android_webkit.dart` を実行し、`android_webkit.g.dart` と `AndroidWebkitLibrary.g.kt` が pigeon により生成
1. example/ ディレクトリで `flutter build apk --debug` を実行し、ビルドが落ちるのを確認
1. WebViewFeature や WebViewSettingsCompat への proxy クラスを作成し `ProxyApiRegistrar.java` に登録
1. webview\_flutter\_android で実際に使えるように `android_webview_controller.dart` に API を追加
1. 追加した dart 及びネイティブコードへのテストを作成

対象の Android 側のメソッドは [androidx.webkit.WebViewSettingsCompat](https://developer.android.com/reference/kotlin/androidx/webkit/WebSettingsCompat) と [androidx.webkit.WebViewFeature](https://developer.android.com/reference/kotlin/androidx/webkit/WebViewFeature) にある (androidx.webkit のライブラリはすでに追加済)。こういったプラットフォームのメソッドを型安全に使う仕組みとして [Pigeon](https://pub.dev/packages/pigeon) というライブラリがあり、上の 2 で MessageChannel を使ったクラスが生成されていた。

## 静的解析やテスト実行

flutter/packages は packages/ 配下に様々なライブラリが管理されている mono repo 構成を取っているので、project root から実行できるように様々な [tools](https://github.com/flutter/packages/tree/main/script/tool) が用意されている。2025/08 時点の開発で主に使ったコマンドは以下。

```bash
# コードのフォーマット
# cd path/to/project_root
dart run script/tool/bin/flutter_plugin_tools.dart format --packages webview_flutter_android

# Android ネイティブコード実装のテスト
dart run script/tool/bin/flutter_plugin_tools.dart native-test --android --packages webview_flutter_android

# dart コードのテスト
dart run script/tool/bin/flutter_plugin_tools.dart dart-test --packages webview_flutter_android

# changelog の生成
dart run script/tool/bin/flutter_plugin_tools.dart update-release-info \
  --version=minimal # 機能追加の場合は minor \
  --base-branch=upstream/main \
  --changelog="description"
```

CI は ci.chromium.org のリソースが使われていた。[.ci.yaml](https://github.com/flutter/packages/blob/main/.ci.yaml) に各 job の定義があるので、何かしら落ちたら該当の job の target\_file を見に行って実行しているスクリプトを見ればよかった。

- - -

README.md 内のコードブロックもメンテナンスする仕組みもあり、手が込んでいる。  
https://github.com/flutter/flutter/blob/master/docs/ecosystem/contributing/README.md#updating-code-excerpt-managed-examples

## この先

Android アプリ開発をしてきて、いかに UI を自在に作れるかと堅牢に作れるかを考える機会が多かったため、そこを Flutter に合わせていくのと、iOS 端末への知識を上乗せすれば生きていける気がする。なのでまずは Flutter Widget をある程度身体になじませる必要がある。  
iOS/Android における ... は Flutter ではなにか、みたいな形で容易にマッピングできる時代なのでありがたい。

堅牢という意味では大局的には MVVM という形をベースに考えることが多かったけれど、M と V のレイヤーはだいたい一緒で、UI からロジックを剥がしてどうより良く管理していくかのやり方を学んでいく。
