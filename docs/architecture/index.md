---
title: アーキテクチャ概要
description: Palleria の全体レイヤー構造、モジュール構成、使用技術スタック
---

# アーキテクチャ概要

Palleria は、Kotlin (Jetpack Compose) による UI・ドメイン層と、Rust による通信・API パース層からなるアーキテクチャで構成されています。

---

## システム構成

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
