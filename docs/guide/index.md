---
title: Palleria の概要
description: Palleria の概要、特徴、アーキテクチャ設計、および技術スタック
---

# Palleria の概要

**Palleria** は Android 13（API レベル 33）以上を対象としたオープンソースの非公式 Pixiv クライアントアプリケーションです。

宣言型 UI フレームワークである **Jetpack Compose** と **Miuix KMP** コンポーネントシステムを採用し、通信およびデータ処理のネイティブコアとして **Rust (UniFFI)** を統合しています。

::: warning
Palleria は非公式の Pixiv クライアントであり、ピクシブ株式会社とは一切関係ありません。
:::

---

## 採用技術スタック

アプリ内で使用されている主要ライブラリおよび動作環境の一覧です。

### アプリケーション基盤
- **開発言語**: Kotlin 2.x, Rust (2021 Edition)
- **UI フレームワーク**: Jetpack Compose (Compose BOM `2026.06.01`)
- **デザインシステム**: Miuix KMP (`0.9.3`)
- **非同期処理**: Kotlin Coroutines & StateFlow (`1.11.0`)
- **画面遷移**: AndroidX Navigation 3 (`1.1.4`)

### ネットワーク & 画像処理
- **API 通信コア**: Rust クレート (`pixiv-api`) + UniFFI JNI バインディング
- **HTTP トランスポート**: OkHttp (`5.4.0`)
- **画像読み込み・キャッシュ**: Coil 3 (`3.5.0`) - GIF / Zip / ネットワークキャッシュ対応
- **Web 認証**: AndroidX Browser Custom Tabs (`1.10.0`)

### ローカルデータ & セキュリティ
- **データベース**: Room (`2.8.4`)
- **設定データストア**: Jetpack DataStore Preferences (`1.2.1`)
- **暗号化**: AndroidX Security Crypto (`1.1.0`) + KeyStore (AES-256-GCM)
- **生体認証**: AndroidX Biometric (`1.4.0-alpha07`)

---

## 主な機能一覧

### 1. 作品閲覧 & リーダー
- **イラスト・マンガ**: ピンチズーム、複数ページ一覧/見開き表示、各種画質切り替え。
- **うごイラ**: Zip フレームデータの非同期ダウンロード、デコード、再生速度・ループ制御。
- **小説**: 縦書き/横書き表示切り替え、フォント・行間・テーマ変更、インライン挿絵表示。
- **ショートフィード**: フルスクリーンでの縦スワイプ作品観賞。

### 2. 高度な検索 & ウォッチリスト
- タグ部分一致 / 完全一致、作品タイトル・キャプション検索、ユーザー検索。
- ブックマーク数フィルター（100 / 500 / 1000 / 5000 / 10000users 入り）。
- お気に入りタグを管理するウォッチリスト（新着件数確認）。

### 3. ダウンロードマネージャー
- 原寸（オリジナル画質）画像の保存。
- 作者名・作品IDによるフォルダ自動分離。
- 1〜5 の並列ダウンロードキュー管理および自動ブックマーク連動。

### 4. セキュリティ & カムフラージュ
- PIN コードおよび生体認証（指紋/顔認証）によるアプリロック。
- 四則演算機能を備えた電卓偽装モード (`CalculatorEngine`)。
- `FLAG_SECURE` によるスクリーンショット防止とタスク一覧画面のマスキング。

### 5. システム連携
- ブックマーク作品のローテーション表示を行うライブ壁紙サービス (`PalleriaLiveWallpaperService`)。
- ホーム画面用ランキング表示および単体イラストウィジェット。

---

## 動作要件

| 項目 | 条件 / 仕様 |
| :--- | :--- |
| **最小 OS** | Android 13 (API Level 33) |
| **ターゲット SDK** | Android 14 (API Level 34) |
| **対応 ABI** | `arm64-v8a`, `armeabi-v7a`, `x86_64` |
| **ライセンス** | GNU General Public License v3.0 (GPL-3.0-only) |

---

## スクリーンショット

| ホーム画面 | 検索画面 | ランキング | ユーザープロフィール |
| :---: | :---: | :---: | :---: |
| <img src="https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/phoneScreenshots/1.png" width="160" alt="Home" /> | <img src="https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/phoneScreenshots/2.png" width="160" alt="Search" /> | <img src="https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/phoneScreenshots/3.png" width="160" alt="Ranking" /> | <img src="https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/phoneScreenshots/5.png" width="160" alt="Profile" /> |
