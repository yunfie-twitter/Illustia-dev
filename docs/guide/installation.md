---
title: インストールガイド
description: 動作環境、パーミッション権限、F-Droid および GitHub からのインストール手順
---

# インストールガイド

## 動作要件

| 項目 | 条件 / 仕様 |
| :--- | :--- |
| **OS バージョン** | Android 13 (API Level 33) 以上 |
| **ターゲット SDK** | Android 14 (API Level 34) |
| **アカウント** | Pixiv アカウント (ログイン時) |
| **ネットワーク** | インターネット接続環境 |

---

## 必要なアプリ権限 (Permissions)

Palleria が要求する権限と使用目的は以下の通りです。

| 権限名 | 目的 |
| :--- | :--- |
| `INTERNET` | Pixiv API との通信および画像データのダウンロード |
| `ACCESS_NETWORK_STATE` | Wi-Fi 接続状態の検知 (Wi-Fi のみダウンロード設定等) |
| `WRITE_EXTERNAL_STORAGE` | 画像データのローカルストレージ保存 (Android 9 以下互換) |
| `READ_MEDIA_IMAGES` | 保存済み画像の読み込みおよびライブ壁紙ソース参照 (Android 13+) |
| `USE_BIOMETRIC` / `USE_FINGERPRINT` | 生体認証（指紋/顔認証）によるアプリロック解除 |
| `POST_NOTIFICATIONS` | ダウンロード進捗通知および自動同期通知の表示 |
| `SET_WALLPAPER` | ライブ壁紙サービスによる壁紙の変更設定 |

---

## インストール手順

### 1. F-Droid リポジトリ経由 (推奨)

F-Droid クライアントまたは Droid-ify 等のサードパーティストア経由でインストール・更新できます。

リポジトリ URL:
```text
https://yunfi.f5.si/Palleria/repo/
```

リポジトリの SHA-256 フィンガープリント:
```text
28A7F64F373AC1AD5FBB4822870E2E07B2B204C7EC71E58CE40F9D54EF2727D9
```

1. F-Droid アプリの「設定」>「リポジトリ」>「追加」を開きます。
2. 上記のリポジトリ URL を入力（または QR コード読み取り）して追加します。
3. リポジトリの同期完了後、検索窓で `Palleria` を検索してインストールします。

---

### 2. GitHub Releases 経由 (単体 APK)

[GitHub Releases](https://github.com/yunfie-twitter/Palleria/releases/latest) ページから直接 APK ファイルを入手して手動インストールします。

1. 最新リリースの `Assets` から `Illustia-v5.0.0-release.apk`（または最新バージョンの APK）をダウンロードします。
2. ダウンロードした APK をタップして開きます。
3. ダイアログが表示された場合、「設定」をタップして「このソースからのアプリを許可」を有効にしてインストールを実行します。

::: info 自動更新確認
GitHub 直接インストールの場合でも、アプリ内の「設定」>「バージョン情報」から最新リリースの有無を確認し、更新ファイルを読み込むことができます。
:::
