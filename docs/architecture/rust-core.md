---
title: Rust Native コア (pixiv-api) 仕様
description: UniFFI を用いた Rust API クライアント、バッファサイズ上限制限、メモリ安全設計
---

# Rust Native コア (`pixiv-api`) 仕様

`rust/pixiv-api` は、Pixiv API のネットワーク通信およびレスポンス JSON のパース・型検証を行うネイティブ Rust クレートです。

---

## なぜ Rust なのか？

1. **ゼロコピーデコード & メモリ削減**:
   巨大な JSON レスポンス（数百件の検索結果や長編小説本文）を直接 JVM ヒープメモリへ巨大文字列として読み込まず、Rust 側の型付き DTO にストリームパースしてからアプリモデルへ最小構造で転送します。
2. **メモリ上限による OOM / クラッシュ防止**:
   不正なレスポンスや過大なデータによるアプリのメモリエラーを防ぐため、厳格なバッファ制限を設けています。
3. **UniFFI による安全な JNI 境界**:
   Mozilla UniFFI により、C/C++ で発生しがちな手動メモリ管理や Null ポインタ参照によるクラッシュのリスクを排除。

---

## バッファ上限仕様表

`pixiv-api` 内で規定されている各データの安全読み込み上限サイズです。

| 対象データ | 上限サイズ | 超過時の動作 |
| :--- | :--- | :--- |
| **JSON レスポンス** | **16 MiB** | デコードストリームバッファ上限。超過時は即時 `ApiException` を発生 |
| **小説本文 (HTML)** | **8 MiB** | 小説本文パースバッファ上限 |
| **HTTP エラー本文** | **64 KiB** | サーバーエラーレスポンス取得上限 |
| **例外スタック詳細** | **4 KiB** | Kotlin 側へ伝播するエラーメッセージ情報量の上限 |

---

## モジュール構造 (`rust/pixiv-api/src/`)

- `lib.rs`: UniFFI インターフェース定義およびエントリポイント
- `client.rs`: HTTP 通信クライアントおよびヘッダー・認証処理
- `models/`: Domain 別 DTO 定義
  - `illust.rs`: イラスト・マンガレスポンスモデル
  - `novel.rs`: 小説レスポンスモデル
  - `user.rs`: ユーザー・プロフィールモデル
  - `ugoira.rs`: うごイラフレームメタデータモデル

---

## Android ABI 出力

`cargo-ndk` によりコンパイルされた以下の `.so` ファイルが `app/src/main/jniLibs`（または `$buildDir/generated/uniffi/jniLibs`）に配置され、実行時に `System.loadLibrary` によりロードされます。

- `arm64-v8a/libpalleria_pixiv_api.so`
- `armeabi-v7a/libpalleria_pixiv_api.so`
- `x86_64/libpalleria_pixiv_api.so`
