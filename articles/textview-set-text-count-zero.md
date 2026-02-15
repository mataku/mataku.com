---
title: setText されている TextView の getLineCount() が 0 になる
date: 2018-05-10T21:42:03+09:00
draft: false
tags:
  - Android
---

X行以上あったらもっと見るを表示して表示部分を省略するみたいなのを組むときに, 雑だけど以下の感じで組んでたら setText していた TextView の高さが意図せず 0 になるので一瞬困った。本来は setText(CharSequence) が反映された TextView の値として取得して欲しい。

https://gist.github.com/mataku/8f09601daf7015166205be11b66c1965

もろもろ set された後に, 描画のための表示計算をする前に行を取得してしまって 0 になっていたので, 以下のように UI スレッドでやってもらうようにすると取得できる。そういえば今回は Epoxy を使用して対象の View を生成しようとしていた。

https://gist.github.com/mataku/cf6fddb6d2c43279aed9b3680b6b8a28

レビューの時に RxBinding 使って, RxTextView.textChanges(TextView) でもいけそうというのがあって確かに…！と思った。けれど今回のケースだと setMaxLines(int) を使って行を指定するため, その中で呼ばれる setText(CharSequence) によって, 無限ループになってしまうためこういう方法に落ち着いた。

困ったときには, お世話になった先輩であるもちこさんの 〜の気持ちになって考えて！！！！ というのを思い出している。View の気持ちが分かっていると, 行数が計算できないと言うことは対象の View が描画できていないはずなのでそこを疑うというステップを導きやすくなる。当たり前だけど TextView の getLineCount() にも普通に書いてあってそうだよな…という感じ。

全体の振る舞いを知るのはめっちゃむずいけど, 極小単位で気持ちを知るのを広げていくと, ログとかも意味を以って脳内でハイライトされることもあるので全てに寄り添っていきたい。
