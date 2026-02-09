---
title: ちょっと前に作った Jetpack Compose UI library を Compose Multiplatform 対応した
date: 2024-04-14T23:09:12+09:00
draft: false
tags:
  - 日常
  - Compose Multiplatform
---

ちょっと前に作った `android:ellipsize="middle"` を Compose でやるための Text component を multiplatform 対応した。まだ iOS/Android のみ。

https://github.com/mataku/MiddleEllipsisText/pull/11

Jetpack Compose のライブラリならば、compose-multiplatform を利用すれば事足りるはずで、依存関係として定義しているものが対応しているかを見れば良い。今回のケースでは文字列操作に android.icu.text.BreakIterator という Android OS に依存したクラスを利用していたため、iOS 実装では org.jetbrains.skiko のものに差し替えた。

```kotlin
// BreakIterator.kt in commonMain
package io.github.mataku.middleellipsistext.internal

interface BreakIterator {
  fun next(): Int

  fun makeCharacterInstance(): BreakIterator

  fun setText(text: String?)

  fun current(): Int

  companion object {
    const val DONE = -1
  }
}

expect fun getBreakIterator(): BreakIterator
```

```kotlin
// BreakIterator.ios.kt in iosMain
package io.github.mataku.middleellipsistext.internal

class IOSBreakIterator : BreakIterator {

  private val instance = org.jetbrains.skia.BreakIterator.makeCharacterInstance()

  override fun next(): Int {
    return instance.next()
  }

  override fun makeCharacterInstance(): BreakIterator {
    return this
  }

  override fun setText(text: String?) {
    instance.setText(text)
  }

  override fun current(): Int {
    return instance.current()
  }
}

actual fun getBreakIterator(): BreakIterator = IOSBreakIterator()
```

```kotlin
// BreakIterator.android.kt in androidMain
package io.github.mataku.middleellipsistext.internal

internal class AndroidBreakIterator : BreakIterator {

  private val instance = android.icu.text.BreakIterator.getCharacterInstance()

  override fun next(): Int {
    return instance.next()
  }

  override fun makeCharacterInstance(): BreakIterator {
    return this
  }

  override fun setText(text: String?) {
    instance.setText(text)
  }

  override fun current(): Int {
    return instance.current()
  }
}

actual fun getBreakIterator(): BreakIterator = AndroidBreakIterator()
```

Compose UI での場合では compose-multiplatform があるのでそんなに考えることは少ない。とはいえロジックがプラットフォームに依存しなければ問題はないし、あったとて UI 関連は skia/skiko にあるかもしれない。各プラットフォームで実装を変えたい場合は commonMain で `expect` をつけておき、プラットフォームのクラス (e.g. iosMain, androidMain) で `actual` を付与して実装すれば良く楽。

KMP で実際にマルチプラットフォーム対応のアプリもしくはロジックを共通化するとなるともっと考えることは増えるけれども、メインターゲットと思われる iOS/Android で考えると Swift/Obj-C との相互運用も命名周りから考慮されているし、いまや事例も多く出てきているので、Kotlin を書くという点で実装自体はそこまで迷わないと思う。ただ UI 周りのパフォーマンスや Gradle の運用を考えると、気軽に新規で試すかとは思えず本当にチーム構成への依存度が高い選択肢だと感じる。個人的にはやる機会はなさそうだけれど、ビジネスロジックまで共通化して UI は各プラットフォームで実装するのが今は良い塩梅かな。ちゃんと運用できているチームはすごい。
