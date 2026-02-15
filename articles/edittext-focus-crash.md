---
title: focus search returned a view that wasn’t able to take focus! のクラッシュと仲良くなる
date: 2018-11-05T22:10:33+09:00
draft: false
tags:
  - Android
---
original post: https://matakucom.medium.com


1 EditText + その下に複数の view のレイアウトで、以下の例外によりクラッシュしているところがあるので調べた。

```shell
Fatal Exception: java.lang.IllegalStateException
focus search returned a view that wasn’t able to take focus!
```

フォームは入力内容によって複数用いられることもあるので、IME の右下にあるボタンを使って次のフォームへ移ることもできる (その際は→ のようなマークになっている) 。EditText が複数あったら自動でそうなる認識だが、その機能が用いられて focusable でない view に移ったことでクラッシュしているようだ。

今回は明示的に imeOptions を何も指定していない EditText を 1 つだけ用いていたが、下に view があるせいか IME のキーが次へを示すキーになっているのを確認した。そのため imeOptions として明示的にIME_ACTION_DONE を指定することで、次に移る view がないことを示し、フォーカスしに行かないようにした。

Android の IME のような、アプリ固有でない共通のコンポーネントをあんまり気にすることがなかったので勉強になった。

### 参考

EditorInfo - Android Developers  
https://developer.android.com/reference/android/view/inputmethod/EditorInfo
