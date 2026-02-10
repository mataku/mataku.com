---
title: minne Android と UI test
date: 2018-11-21T22:15:06+09:00
draft: false
tags:
  - Android
---


original post: https://matakucom.medium.com

- - - 

今担当している minne Android で網羅的な動作テストを定期的にしてもらうために Firebase Test Lab による Robo test を導入しました。毎日 10 時に元気に定期実行されています。

Robo test の大まかな内容については、firebase による以下のページをご確認ください。詳細については割愛します。

https://firebase.google.com/docs/test-lab/robo-ux-test

## 狙い

develop ブランチの内容の網羅的な動作確認を、様々な OS 上で定期的にすることで予期せぬクラッシュを発見したい

## 背景

Git-flow で開発しているので、日々開発されたものたちが develop ブランチへマージされています。

その内容はレビューされた内容ではありますが、リリース前検証にて影響範囲を見誤り、クラッシュして出戻りした経験がありました。それを事前に検知するための要素の 1 つとして導入しています。robo test を採用したのは、試しにやってみたところ網羅的に画面を辿ってくれる様子が確認できたためです。

元々 minne では、Model-View-Presenter アーキテクチャに沿って開発されており、model 層及び presenter のユニットテストを充実させる方向に沿ってアプリケーションの堅牢性を高める方針がチームにあります。それが浸透して来たのをきっかけに、2017 年当時僕らの体感として変更に弱く安定しなかったという理由で UI test (AndroidTest) を 2017 年末くらいから使用しなくなった流れがありました。

## 運用方針

Pull request 上での CI のワークフローとは切り離して考えています。

現状ユニットテストや静的解析、人と機械的なレビュー (Danger) によってアプリの品質はある程度保たれていると考えており、リリース前に見つかるような不具合の気付きを促す要素として考えているためです。そのため、定期実行するようにしてます。実行自体が安定して一瞬で終わるのであればワークフローとして組み込めそう。

ちなみに、CI の cron job として、gcloud コマンドを用いて実行しています。

```bash
# Example
gcloud firebase test android run \
  --type robo \
  --app /path/to/apk \
  --device model=taimen,version=26,locale=ja,orientation=portrait  \
  --device model=taimen,version=26,locale=ja,orientation=landscape  \
  --robo-directives text:login_password=$TEST_ACCOUNT_PASSWORD,text:login_username=$TEST_ACCOUNT_USERNAME,click:login_button= # ログイン情報 \
  --timeout=5m
```

## この先

定期実行する方針はそのままにして上記の robo test に加え、購入といった基幹的な機能のみを対象にして instrumentation test を動かそうと考えています。

UI test をするなら普段の機能の担保もユニットテスト書かずにそれで賄えば良いのでは？？？？というのもあるかもしれません。

ただそれで行くと Activity の肥大化のように、クラス内での責務の明確化が行われない作りに運用面でのコストが高まる恐れが出てきます。基本的にはユニットテストによるロジックの担保の方針は変えずに、あくまで気付きの補助の役割として直近は進める予定です。
