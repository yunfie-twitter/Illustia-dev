---
title: Rust Native コア (pixiv-api)
description: UniFFI を用いた Rust API クライアント、型バインディング、メモリ安全設計
---

# Rust Native コア (`pixiv-api`)

Pixiv API との通信トランスポート、レスポンス JSON の検証・デコード処理は `rust/pixiv-api` クレートで実装されています。UniFFI を介した型付き Kotlin バインディング（`nativebridge`）を通して利用されます。

---

## 設計上のポイント

1. **直接的な DTO 変換**: レスポンス本文を文字列として Kotlin (JVM) へ渡さず、Rust 側で `serde` により DTO へパースしてからアプリモデルへ橋渡しします。
2. **バッファサイズの安全制御**: メモリ超過（OOM）や例外クラッシュを防止するため、処理バッファに上限が設定されています。

---

## 制限値仕様

| 対象データ | 上限サイズ | 動作仕様 |
| :--- | :--- | :--- |
| **JSON レスポンス** | **16 MiB** | デコードバッファ上限。ストリームパース時に超過した場合は処理中断 |
| **小説本文 (HTML)** | **8 MiB** | 読み込みバッファ上限 |
| **HTTP エラー本文** | **64 KiB** | エラー解析用読み取り上限 |
| **例外詳細情報** | **4 KiB** | Kotlin 側へ返却する例外メッセージ上限 |

---

## ビルドおよびツールチェーン

- **UniFFI**: UDL / proc-macro から Kotlin のインターフェースコードを生成。
- **cargo-ndk**: Android SDK の NDK を自動検出し、以下の 3 アーキテクチャ向け `.so` バイナリを出力。
  - `aarch64-linux-android`
  - `armv7-linux-androideabi`
  - `x86_64-linux-android`
- Gradle の `preBuild` タスクがこれらをビルドパイプラインの一部として実行します。
