---
title: ライブ壁紙 & ウィジェット
description: ライブ壁紙サービスとホーム画面用ウィジェットの仕様
---

# ライブ壁紙 & ウィジェット

Android システム機能と連携したライブ壁紙サービスおよびウィジェットの仕様です。

---

## ライブ壁紙サービス (`PalleriaLiveWallpaperService`)

端末の壁紙としてイラストを定期的に切り替えて表示する Android LiveWallpaper サービスです。

### 設定パラメータ (`AppSettings`)
- `liveWallpaperSource`: ソースの選択 (`all` / `bookmarks` / `folder`)
- `liveWallpaperIntervalMinutes`: 切り替え間隔（分単位。デフォルト 60 分）
- `liveWallpaperOrder`: 再生順序 (`random` / `order`)
- `liveWallpaperScaleMode`: 画面フィット方式 (`cover` / `fit`)
- `liveWallpaperCrossfade`: 画像切り替え時のクロスフェード遷移 (Boolean)
- `liveWallpaperExcludeSensitive`: センシティブ（R-18）作品の除外 (Boolean)

---

## ホーム画面ウィジェット

### 1. イラストウィジェット (`IllustWidgetProvider`)
- お気に入り作品やランダムイラストをホーム画面に常時表示。
- ウィジェットタップ時に Palleria アプリ内の該当イラスト詳細画面へ直接遷移。

### 2. ランキングウィジェット (`RankingWidgetProvider`)
- 今日の Pixiv デイリーランキング TOP 10 作品を取得し、スライド表示。
- 定期バックグラウンド更新により、最新データを維持。
