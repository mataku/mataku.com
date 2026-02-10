---
title: Android の apk に付与される権限の変化を検知する
date: 2018-05-30T21:56:54+09:00
draft: false
tags:
  - Android
---

Android の apk に付与される権限に変化があったかどうかのチェックを楽にしたいので Danger plugin を作っています。主にライブラリ導入, 追加の際に見落としがちなチェックとして機能することを想定しています。

https://github.com/mataku/danger-android_permissions_checker

## 背景

ライブラリを導入, アップデートする際に, 公開されているライブラリなどであれば内容を確認しやすいけど, Google Play services のようにリリースノートおよびドキュメントからしか情報がない場合もあり得ますね。

その際にもし記載されていない変な権限が付与されていたとしても, 端末に apk をインストールするまで分からないとなると, 確認には人の温かみある作業に依存してしまうのを感じていました。

アプリが使用する権限はユーザへの不安の要因の 1 つになり得るためです。

## Danger plugin + aapt

そこで CI でのワークフロー (Danger) に導入する形で自動で検知できる仕組みを作る方向で対応しました。生成される apk (例えば Bitrise でいうと $BITRISE_APK_PATH, なければ直接指定) とあらかじめ生成しておいた権限のリストを指定します。

```ruby
# Dangerfile
android_permissions_checker.check(
  apk: '/path/to/apk',
  permission_list_file: '/path/to/generated_permission_list'
)
```

```
# permissions.txt
package: com.mataku.amazingapp
permission: com.mataku.amazingapp.permission.C2D_MESSAGE
uses-permission: name='android.permission.INTERNET'
```

生成された apk と現時点で期待されるものを比較する必要があり, 既存の権限のリストに関しては情報がないので, リストをあらかじめ作ってもらって指定する形にしました。aapt d permissions /path/to/apk で生成しておきます。

CI でのワークフローで生成された apk から、権限を aapt でパースして変更があったら教えてくれるようにしているので aapt コマンドを CI サービス上で使えるようにパスを通す必要があります。もし何らかの変更があり, それが期待するものであれば, リストも一緒に更新してもらう流れです。


![](/images/check-android-apk-permission-danger-result.png "Danger によるコメント")

## メモ

試していないので Play Console とかでアップロード時に気づけるかも知れないなと思ったけど, 早く気付けるに越したことはないですね。自動化していると見逃される可能性も考えられます。

依存する部分がちょっとあるため, バリデーションをちょい充実することでカバーしているけど, 依存度を減らせるなら減らすにこれまた越したことはないですね。これ指定せずにここから取れるとかあれば教えて欲しいです。
