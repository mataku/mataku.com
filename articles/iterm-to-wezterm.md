---
title: iTerm2 から WezTerm に変えた
date: 2024-10-24T14:51:00+09:00
draft: false
tags:
  - dotfiles
---

https://github.com/mataku/dotfiles/tree/develop/wezterm

設定ファイルを管理しやすい形にしたくて WezTerm にした。NeoVim の設定ファイルを init.vim から気合いで init.lua に書き換えた身としては、Lua で設定ファイルを管理する WezTerm はお得感がある。使用感は特に変わらない。

https://wezfurlong.org/wezterm/ 配下が充実していて WezTerm API をどう使うかは探せるし、そこにどう Lua を使ってデータを加工するかも、やりたいことが決まっていれば AI に助けてもらいやすいので無事移行を終えられた。colorscheme も公式にページが用意されていて、数がめちゃめちゃあるのがおもしろい。見た目は今のままが良いなということで、iTerm2 から export したものをそのまま利用している。

- - -

init.vim から置き換える際に、dein.vim から lazy.nvim に変えたり、補完で利用していた coc.nvim を nvim-lspconfig + nvim-cmp に変えることで、結果的に Lua ファイルのみで管理できるように変更した。dotfiles を見直す際には、一旦やると全部やっちゃおうとなりがち。

もろもろ変更した後に iTerm2 には Python API があるのを知った。profile は JSON で import/export していたので全部できるなら便利かな。
