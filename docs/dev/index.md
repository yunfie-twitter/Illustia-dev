---
title: アーキテクチャ概要 & レイヤー設計
description: Palleria の開発者向け技術ドキュメント、モジュール構造、データフロー
---

# アーキテクチャ概要 & レイヤー設計

本ドキュメントは Palleria のソースコード構造、モジュール設計、データフローに関する開発者・技術者向けリファレンスです。

---

## 全体レイヤー構成図

```
 ┌────────────────────────────────────────────────────────┐
 │                   UI Layer (Kotlin)                    │
 │  - Jetpack Compose + Miuix KMP Components              │
 │  - Navigation / ViewModels (StateFlow / Coroutines)    │
 └───────────────────────────┬────────────────────────────┘
                             │
 ┌───────────────────────────▼────────────────────────────┐
 │                Data & Local Storage Layer              │
 │  - Room DB (Account, View/Search History, SavedIllust) │
 │  - DataStore (AppSettings / SettingsStore)             │
 │  - PallaSync Engine (暗号化バックアップ & 同期)        │
 └───────────────────────────┬────────────────────────────┘
                             │ (JNI via UniFFI)
 ┌───────────────────────────▼────────────────────────────┐
 │                 Rust Core (pixiv-api)                  │
 │  - Transport Bridge (OkHttp / Rust Client)             │
 │  - Serde JSON Decoder (バッファサイズ上限制御)         │
 │  - DTO & Domain Model Mapping                          │
 └────────────────────────────────────────────────────────┘
```

---

## 主要パッケージ構成 (`com.yunfie.illustia`)

- `account`: ログイン処理、OAuth2 トークン取得・管理
- `data`: リポジトリ層 (`IllustiaRepository`, `ManagedDataRepository`), API クライアント (`RustPixivHttpClient`), Room 連携
- `models`: UI およびデータレイヤーで共有するデータモデル群 (`Illust`, `UserProfile`, `SearchTarget` 等)
- `nativebridge`: UniFFI により生成された Rust との JNI バインディング層
- `pallasync`: 設定およびステートの暗号化同期・復元エンジン
- `settings`: `AppSettings` 定義、DataStore 処理 (`SettingsStore`), Room DB エンティティ定義 (`settings/db/`)
- `ui`: Compose 画面 (`ui/screens/`) および共通 UI コンポーネント (`ui/components/`)
- `viewmodel`: 各画面の StateFlow / State を保持する ViewModel
- `wallpaper`: ライブ壁紙サービス (`PalleriaLiveWallpaperService`)
- `widget`: アプリウィジェットプロバイダ (`IllustWidgetProvider`, `RankingWidgetProvider`)
