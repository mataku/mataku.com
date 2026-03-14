---
title: Betterment/alchemist での閾値設定
date: 2026-03-14T22:24:08+09:00
tags:
  - Flutter
  - Golden test
---

[Betterment/alchemist](https://github.com/Betterment/alchemist) を使って golden test を導入した。その際に、コンポーネント外のピクセル差分でテストが落ちることがあったので、diff に関する閾値を設定できるようにした。

Pull request: https://github.com/Betterment/alchemist/pull/176

## Context

携わっているプロダクトでデザインシステムがちゃんと整備される体制ができつつあるので、まずは基本となる UI コンポーネントが変更によって壊れないかをチェックするために入れた。

flutter_test を使う、でも良かったが、alchemist の方が、テスト全体で適用されるの設定のやりやすさや `goldenTest` さえ呼べば描画と画像比較を行ってくれるように、かゆいところに手が届くものだったので採用。実質的に flutter_test のラッパーではあるので、どうにかしやすいという点もある。

## CI tests

https://github.com/Betterment/alchemist#about-platform-tests-vs-ci-tests

Platform tests ではなく CI tests の設定で動かしており、Ahem というフォントで置き換わることでフォントレンダリングによる環境の差異でテストができるだけ落ちないようにする方向で運用し始めた。Ahem 自体は flutter_test でも使われる。

![](/images/banner_golden_test.png "テキストを使った golden image")


モバイルアプリという観点だと、ちゃんと作るなら iOS/Android でフォントが異なるので、それをちゃんと検証したほうが良いだろうかというのは悩んでいる。ただ、Text の font size や line-height みたいなプロパティによって描画領域はちゃんと変わって差分として検出されるし、テスト自体がそもそも面倒なので、フォントは figma みたいなところで確認できれば良いのかなと落ち着いている。

## 画像生成環境の差異によるテスト失敗

画像生成は macOS, テストは GitHub Actions の ubuntu-latest でやっているが、CI 上のみコンポーネント外のほんのわずかな差分でテストが落ちることがあった。

前職の時に Android アプリで golden test を導入した際にも起こっていた。その際には [Roborazzi](https://github.com/takahirom/roborazzi) の `changeThreshold` をめちゃ小さい値で設定しておいて、ほんのわずかな差分であれば無視するようにしていたのを思い出したので、閾値を設定できるようにした。

[Issue](https://github.com/Betterment/alchemist/issues/90) の方で画像生成と比較を行う環境を揃えるのが良いみたいなコメントもあったが、普段運用するにあたりそれ用の workflow 作って、呼び出すみたいにするのは面倒。

## おわりに

という LT をしたのだが、LT 前に pull request がマージされていればベストだった。あと LT やってと言われたので当然 5 分と思っていたが、直前にタイムスケジュール見て 10 分枠だったので、文化を知った。