---
title: setText されている TextView の getLineCount() が 1 になる
date: 2018-05-26T21:48:32+09:00
draft: false
tags:
  - Android
---

https://mataku.com/articles/textview-set-text-count-zero/

これで対策したぞと思ってたら, 端末およびタイミングによって今度は 1 になることがあって, view の inflate のタイミングの問題で起きているのではと。端末によっては全く問題なく動くものもあるのが厄介だったという感じ。

https://developer.android.com/reference/android/widget/TextView.html#getLineCount%28%29

TextView の getLineCount() は, まだ描画されていなければ 0 が返るので, 描画しきっていないタイミングによって起こってしまうような事案になっているのかなと考えた。

そのため, レイアウトができあがるのをが終了した時点でやるのが確実だということで, OnGlobalLayoutListener をセットし, onGlobalLayout 内で処理を行い, 正確な値を取得しその値を以て処理を行うことで, ひとまず事なきを得た。描画周りは腰を据えないと本当に雰囲気でやってしまいがちなので怖い。

https://gist.github.com/mataku/3b2e48dbd21a0434b132faae6d17de7a
