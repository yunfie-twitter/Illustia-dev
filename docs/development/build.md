---
title: ビルド
description: Palleriaをローカルでビルドする手順。
---

# ビルド

## 必要なもの

- Git
- Android Studio
- JDK 17
- Android SDK / NDK
- Rustツールチェーン
- cargo-ndk

アプリのGradleタスクは、RustライブラリのビルドとUniFFIバインディング生成も実行します。

## リポジトリを取得

```bash
git clone https://github.com/yunfie-twitter/Palleria.git
cd Palleria
```

## Rustターゲットを準備

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo install cargo-ndk --locked
```

Android StudioのSDK ManagerからNDKもインストールし、ローカル環境から利用できるようにしてください。

## デバッグAPK

macOSまたはLinux:

```bash
./gradlew :app:assembleDebug
```

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

生成物は通常、`app/build/outputs/apk/`以下に保存されます。

## リリースAPK

リリース署名には次の環境変数が必要です。

```text
KEYSTORE_PATH
KEYSTORE_PASSWORD
KEY_ALIAS
KEY_PASSWORD
```

署名情報とキーストアをリポジトリへコミットしないでください。

```bash
./gradlew :app:assembleRelease
```

## ドキュメント

ドキュメントサイトにはNode.js 20以降を使用します。

```bash
npm install
npm run docs:dev
```

本番用の静的ファイルを生成するには次を実行します。

```bash
npm run docs:build
```
