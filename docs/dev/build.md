---
title: ビルド & 環境構築パイプライン
description: Android SDK, NDK, UniFFI, Rust ツールチェーンのビルドパイプライン
---

# ビルド & 環境構築パイプライン

## 開発環境要件

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

## 2. Gradle ビルドパイプライン

Gradle の `preBuild` タスクにより、Rust コードのコンパイルおよび UniFFI による Kotlin バインディングの自動生成が実行されます。

### 自動呼び出しされる内部 Gradle タスク
1. `buildUniFfiHost`: ホスト環境用 Rust ライブラリのビルド。
2. `generateUniFfiBindings`: UniFFI `uniffi-bindgen` を呼び出し、Kotlin インターフェースコードを `$buildDir/generated/uniffi/kotlin` へ出力。
3. `buildRustAndroid`: `cargo-ndk` を用いて 3 つの ABI (`arm64-v8a`, `armeabi-v7a`, `x86_64`) 用の `libpalleria_pixiv_api.so` をビルドし `$buildDir/generated/uniffi/jniLibs` へ出力。
4. `buildPallaSyncCoreAndroid`: `pallasync-core` クレートの `.so` バイナリビルド。

---

## 3. ビルドコマンド

### Debug APK
```bash
./gradlew :app:assembleDebug
```

### Release APK
```bash
export KEYSTORE_PATH="/path/to/release.keystore"
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="your_key_alias"
export KEY_PASSWORD="your_key_password"

./gradlew :app:assembleRelease
```
出力結果: `app/build/outputs/apk/release/Illustia-v5.0.0-release.apk`
