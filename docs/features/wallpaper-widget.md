---
title: ライブ壁紙 & ウィジェット仕様
description: ライブ壁紙サービス、画像ローテーション制御、ホーム画面ウィジェットプロバイダ
---

# ライブ壁紙 & ウィジェット仕様

Android システムと統合されたライブ壁紙およびホーム画面ウィジェットに関する詳細仕様です。

---

## ライブ壁紙サービス (`PalleriaLiveWallpaperService`)

Android の `WallpaperService` を拡張し、Pixiv 作品やローカル保存画像を定期的に壁紙として描画・切り替えるバックグラウンドサービスです。

### 制御パラメータ (`AppSettings`)

| 設定キー | 型 | 初期値 | 説明 |
| :--- | :--- | :--- | :--- |
| `wallpaperPlaylistEnabled` | `Boolean` | `false` | ライブ壁紙機能の有効化 |
| `liveWallpaperSource` | `String` | `"all"` | 画像ソース (`all` / `bookmarks` / `folder`) |
| `liveWallpaperSourceFolder` | `String` | `""` | カスタムフォルダ指定時のローカルパス |
| `liveWallpaperIntervalMinutes`| `Int` | `60` | 画像切り替えインターバル時間（分） |
| `liveWallpaperOrder` | `String` | `"random"` | 切り替え順序 (`random` / `order`) |
| `liveWallpaperScaleMode` | `String` | `"cover"` | 画面フィッティング (`cover` / `fit`) |
| `liveWallpaperBackground` | `String` | `"black"` | 余白背景色 (`black` / `white` / `blur`) |
| `liveWallpaperCrossfade` | `Boolean` | `true` | 切り替え時のクロスフェードアニメーション |
| `liveWallpaperExcludeSensitive`| `Boolean` | `true` | R-18 センシティブ作品の壁紙除外 |

---

## ホーム画面ウィジェット

`AppWidgetProvider` を継承した 2 種類のホーム画面ウィジェットを提供します。

### 1. イラストウィジェット (`IllustWidgetProvider` / `IllustWidgetConfigureActivity`)
- **機能**: ブックマーク作品や特定タグのイラストを 2x2 / 4x4 サイズで配置。
- **データ保持 (`IllustWidgetStore`)**: 設定された表示モード、作品 ID、更新タイマーを Preferences に保持。
- **タップアクション**: ウィジェットタップ時に Palleria アプリを起動し、該当イラストの詳細画面へ直接ディープリンク遷移。

### 2. ランキングウィジェット (`RankingWidgetProvider`)
- **機能**: Pixiv のデイリーランキング TOP 10 作品のサムネイルとタイトルを 4x2 サイズでスライド表示。
- **更新ロジック**: `WorkManager` 定期タスクと連携し、毎日バックグラウンドでランキングデータを最新に更新。
