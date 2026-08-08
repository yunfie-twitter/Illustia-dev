---
title: ビルド手順
description: Android SDK と Rust NDK を使用したビルド手順
---

# ビルド手順

Palleria は Kotlin と Rust のハイブリッド構造になっています。ビルドを行うには Android 開発環境と Rust クロスコンパイル環境が必要です。

---

## 必須環境

- **JDK**: Java 21 以上
- **Android SDK**: API Level 34 (Build-Tools 34.0.0 以上), NDK (最新版)
- **Rust**: stable ツールチェーン (`cargo`, `rustc`)
- **cargo-ndk**: Cargo 用 Android NDK ビルドヘルパー
- **Git**

---

## 1. Rust ネイティブターゲットの設定 (初回のみ)

Android の各 CPU アーキテクチャ向けターゲットを追加し、`cargo-ndk` をインストールします。

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo install cargo-ndk --locked
```

::: info 自動ビルド
Gradle の `preBuild` タスク実行時に UniFFI による Kotlin バインディング生成と各 ABI (`.so`) のコンパイルが自動的に行われます。
:::

---

## 2. リポジトリの取得

```bash
git clone https://github.com/yunfie-twitter/Palleria.git
cd Palleria
```

---

## 3. Gradle によるビルド

### Debug APK のビルド

Linux / macOS:
```bash
./gradlew :app:assembleDebug
```

Windows (PowerShell):
```powershell
.\gradlew.bat :app:assembleDebug
```

出力先: `app/build/outputs/apk/debug/app-debug.apk`

---

### Release APK のビルドと署名設定

リリースビルドには環境変数で署名用キーストア情報を渡す必要があります。

設定する環境変数:
- `KEYSTORE_PATH`: キーストアファイルの絶対パス (`.jks` / `.keystore`)
- `KEYSTORE_PASSWORD`: キーストアのパスワード
- `KEY_ALIAS`: キーのエイリアス名
- `KEY_PASSWORD`: キーのパスワード

Linux / macOS:
```bash
export KEYSTORE_PATH="/path/to/release.keystore"
export KEYSTORE_PASSWORD="your_password"
export KEY_ALIAS="your_alias"
export KEY_PASSWORD="your_password"

./gradlew :app:assembleRelease
```

Windows (PowerShell):
```powershell
$env:KEYSTORE_PATH = "C:\path\to\release.keystore"
$env:KEYSTORE_PASSWORD = "your_password"
$env:KEY_ALIAS = "your_alias"
$env:KEY_PASSWORD = "your_password"

.\gradlew.bat :app:assembleRelease
```

出力先: `app/build/outputs/apk/release/app-release.apk`

---

## 4. Rust クレート (`pixiv-api`) の単体テスト

Rust 側の動作検証およびコード品質チェックを行う場合:

```bash
cd rust/pixiv-api
cargo fmt --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-features
```
