# Android-Serial
AndroidスマートフォンとRaspberry pi pico の通信プログラム

## これは何？

AndroidスマートフォンとRaspberry pi pico でUART通信するプログラムです．

AndroidアプリとRaspberry pi picoのイメージがビルドできます．

なお、本アプリは、Serial通信デバイスとして `FT231X` を搭載しているものを対象としています．

フォルダ構成は下記の通りです．

- AndroidアプリのAndroidStudioProject
  - `FT231XSerialConnect` ディレクトリ以下
- Raspberry pi picoの VSCode Extension 用ファイル
  - `uart_advanced` ディレクトリ以下
  - 公式のexampleに修正を加えています．

## 必要なもの

- Androidスマートフォン
- Raspberry pi PICO
- USB-Serial変換アダプタ
  - [こちら](https://www.switch-science.com/products/6455)で購入できます．
