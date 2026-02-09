---
title: Compose の Modifier.Node で多重タップ防止
date: 2024-12-25T12:34:52+09:00
draft: false
tags:
  - Compose
  - Modifier
  - throttle
---

`Modifier.Node` を用いて指定時間以内の多重クリック防止処理を書いた。以前は `Modifier.composed` を用いて書いていたものの後継に当たる。


![](/images/compose-multi-tap-prevention-click.gif)

大切なことはすべて https://developer.android.com/develop/ui/compose/custom-modifiers にあった。

## Modifier.Node と対象の DelegatableNode を implement する

上記のページに載っている中で、今回はタップに関するイベントをフックしたいので、`PointerInputModifierNode` を選んだ。

```kotlin
class ThrottleClickableNode(
) : PointerInputModifierNode, Modifier.Node() {

  override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
    // ...
  }

  override fun onCancelPointerInput() {
    // ...
  }
}
```

### onPointerEvent, onCancelPointerInput

PointerInputModifierNode にはタップに関するイベントを処理する用の onPointerEvent と主に `ACTION_CANCEL` を処理する用の onCancelPointerInput がある。PointerEvent の type には Press, Release, Move, Scroll などがあり、今回は画面を押す Press から、離すまでの Release を onPointerEvent 内でハンドリングすれば良い。

PointerEventPass は Initial, Main, Final と遷移していく。特に考慮することがなければ Main の場合にフックするで良いと思う。

最初のイベントから指定期間無視したいので、Rx の throttle パターンに沿うように実装。onCancelPointerInput 時には特に特別なことはしないので状態をリセットした。

```kotlin
class ThrottleClickableNode(
  // 対象の時間
  var throttleTimeMs: Long,
  // 対象のアクション
  var onClick: () -> Unit,
  var interactionSource: MutableInteractionSource?,
) : PointerInputModifierNode, Modifier.Node() {
  private var invokable = true
  private var lastPress: PressInteraction.Press? = null

  override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
    if (invokable) {
      when (pointerEvent.type) {
        // タップした
        PointerEventType.Press -> {
          // 領域外でのマルチタップを防ぐため、タップされた領域を Press 時にチェック
          if (pass == PointerEventPass.Main && positionWithinBounds(lastChange.position, bounds)) {
            val press = PressInteraction.Press(pointerEvent.changes.last().position)
            interactionSource?.tryEmit(press)
            lastPress = press
          }
        }

        // タップした指が離れた
        PointerEventType.Release -> {
          if (pass == PointerEventPass.Main) {
            val lastChange = pointerEvent.changes.lastOrNull() ?: return
            if (invokable && lastPress != null) {
              invokable = false
              coroutineScope.launch {
                delay(throttleTimeMs)
                invokable = true
              }
              // Check if the Release is within the tapped area
              if (positionWithinBounds(lastChange.position, bounds)) {
                onClick.invoke()
              }
              interactionSource?.tryEmit(
                PressInteraction.Release(lastPress!!)
              )
              lastPress = null
            }
          }
        }

        // タップしたまま指 (ポインタ) が動いた
        PointerEventType.Move -> {
          if (pass == PointerEventPass.Main && lastPress != null) {
            val lastChange = pointerEvent.changes.lastOrNull() ?: return
            if (!positionWithinBounds(lastChange.position, bounds)) {
              interactionSource?.tryEmit(
                PressInteraction.Cancel(lastPress!!)
              )
              lastPress = null
            }
          }
        }
      }
    }
  }

  override fun onCancelPointerInput() {
    if (lastPress != null) {
      interactionSource?.tryEmit(
        PressInteraction.Cancel(lastPress!!)
      )
    }
    lastPress = null
    invokable = true
  }
}

// 範囲内にいるかチェックするための関数
private fun positionWithinBounds(
  position: Offset,
  bounds: IntSize
): Boolean {
  return position.x >= 0 && position.x <= bounds.width &&
    position.y >= 0 && position.y <= bounds.height
}
```

`PointerEventType.Move` もチェックしているのは、タップしたまま領域外に動いた場合は ripple effect を終えたいので、領域外に出たかをチェックするため。タップした後一定期間タップイベントを無視したかったので、その期間を外から指定するための `throttleTimeMs`, ripple effect のための interactionSource をもらうようにした(後述)。

## ModifierNodeElement

> A ModifierNodeElement is an immutable class that holds the data to create or update your custom modifier

Modifier に chain できるように、作成した Node を扱うための入れ物を data class で作成する。ページにも記載してあるが、class の場合は equals を自前で実装しないと値が同じでもインスタンス比較で false になってしまうので、data class が楽。

```kotlin
private data class ThrottleClickableElement(
  val throttleTimeMs: Long,
  val onClick: () -> Unit,
  val interactionSource: MutableInteractionSource?,
) : ModifierNodeElement<ThrottleClickableNode>() {
  override fun create(): ThrottleClickableNode {
    return ThrottleClickableNode(throttleTimeMs, onClick, interactionSource)
  }

  override fun update(node: ThrottleClickableNode) {
    node.throttleTimeMs = throttleTimeMs
    node.onClick = onClick
    node.interactionSource = interactionSource
  }
}
```

## Modifier factory

毎回 `then 作成したModifierNodeElement` とするのはだるいので、より良く chain できる道を作る。

```kotlin
fun Modifier.throttleClickable(
  throttleTimeMs: Long = 500L,
  onClick: () -> Unit,
  interactionSource: MutableInteractionSource? = null,
  indication: Indication? = null
): Modifier {
  return this
    .then(
      if (interactionSource != null && indication != null) {
        Modifier
          .indication(
            interactionSource = interactionSource,
            indication = indication
          )
      } else {
        Modifier
      }
    )
    .then(ThrottleClickableElement(throttleTimeMs, onClick, interactionSource))
}
```

また、押せない場合にはフィードバックをしたくなかったので、Modifier の indication をこちらでコントロールする必要があった。そのため引数で indication が与えられたらセットしつつ、`PointerInputModifierNode` の `onPointerEvent` で ripple effect を発火できるように interaction を発行した。特に考慮しないならカスタマイズせずに Modifier の clickable のブロック内で throttle や debounce 処理しても良いかも。

- - -

ということでこのようになった。
https://gist.github.com/mataku/34602318cb7f00df8bf379f2eb2b6c43


```kotlin
// Usage
Text(
  text = "Click",
  modifier = Modifier
    .throttleClickable(
      throttleTimeMs = 500L,
      onClick = onClick
    )
    .padding(
      horizontal = 16.dp,
      vertical = 8.dp,
    )
)
```

slackhq/compose-lints では Modifier.composed の利用でエラーになるように lint rule があり便利だった。
