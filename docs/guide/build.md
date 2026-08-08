---
title: ビルド & 環境構築ガイド
description: Android SDK, NDK, UniFFI, Rust ツールチェーンのビルドパイプライン
---

# ビルド & 環境構築ガイド

Palleria は Kotlin (Jetpack Compose) と Rust (NDK Native) から構成されるマルチ言語プロジェクトです。

---

## 開発環境と前提条件

- **JDK**: OpenJDK / Temurin 21 以上
- **Android SDK**: API Level 34 (Android 14) / Compile SDK 37
- **NDK**: Android NDK (最新版)
- **Rust**: 1.80 以上 (stable)
- **Cargo-NDK**: Cargo 用 Android NDK クロスコンパイルツール
- **Git**

---

## 1. Rust NDK ツールチェーンの設定

Android の各ターゲットアーキテクチャ向け標準ライブラリおよび `cargo-ndk` をインストールします。

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo install cargo-ndk --locked
```

---

## 2. リポジトリのクローン

```bash
git clone https://github.com/yunfie-twitter/Palleria.git
cd Palleria
```

---

## 3. Gradle ビルドパイプライン

Gradle の `preBuild` タスクにより、Rust コードのコンパイルおよび UniFFI による Kotlin バインディングの自動生成が実行されます。

### 自動呼び出しされる内部 Gradle タスク
1. `buildUniFfiHost`: ホスト環境用 Rust ライブラリのビルド。
2. `generateUniFfiBindings`: UniFFI `uniffi-bindgen` を呼び出し、Kotlin インターフェースコードを `$buildDir/generated/uniffi/kotlin` へ出力。
3. `buildRustAndroid`: `cargo-ndk` を用いて 3 つの ABI (`arm64-v8a`, `armeabi-v7a`, `x86_64`) 用の `libpalleria_pixiv_api.so` をビルドし `$buildDir/generated/uniffi/jniLibs` へ出力。
4. `buildPallaSyncCoreAndroid`: `pallasync-core` クレートの `.so` バイナリビルド。

---

## 4. ビルドコマンド

### Debug APK のビルド

Linux / macOS:
```bash
./gradlew :app:assembleDebug
```

Windows (PowerShell):
```powershell
.\gradlew.bat :app:assembleDebug
```

出力結果: `app/build/outputs/apk/debug/app-debug.apk`

---

### Release APK のビルドと署名設定

リリースビルドを行う場合は、署名用キーストアの環境変数を設定してください。

#### 必要な環境変数
- `KEYSTORE_PATH`: キーストアファイルの絶対パス (`.jks` / `.keystore`)
- `KEYSTORE_PASSWORD`: キーストアパスワード
- `KEY_ALIAS`: キーのエイリアス名
- `KEY_PASSWORD`: キーのパスワード

Linux / macOS:
```bash
export KEYSTORE_PATH="/path/to/release.keystore"
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="your_key_alias"
export KEY_PASSWORD="your_key_password"

./gradlew :app:assembleRelease
```

Windows (PowerShell):
```powershell
$env:KEYSTORE_PATH = "C:\path\to\release.keystore"
$env:KEYSTORE_PASSWORD = "your_keystore_password"
$env:KEY_ALIAS = "your_key_alias"
$env:KEY_PASSWORD = "your_key_password"

.\gradlew.bat :app:assembleRelease
```

出力結果: `app/build/outputs/apk/release/Illustia-v5.0.0-release.apk`

---

## 5. Rust 単体テスト・ベンチマーク

Rust クレート単体でのテストやコードフォーマット確認を行う場合:

```bash
cd rust/pixiv-api
cargo fmt --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test --all-features
cargo bench --features bench --bench illust_decode
```
