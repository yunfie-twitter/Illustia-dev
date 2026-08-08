---
title: 作品閲覧 & ビューア詳細
description: イラスト・マンガ・小説・うごイラ・ショートフィードの表示仕様および設定
---

# 作品閲覧 & ビューア詳細

Palleria は多様なメディア表現（静止画、マルチページ、Zip アニメーション、文章コンテンツ、縦スワイプフィード）に対応した専用ビューアを搭載しています。

---

## 画面およびフィード構成

### 1. ホームタブ一覧
- **おすすめ (Home)**: アルゴリズムに基づく推奨作品の一覧。
- **フォロー中**: フォロー中ユーザーの新着投稿タイムライン。
- **ランキング (`RankingScreen`)**:
  - デイリー (Daily) / ウィークリー (Weekly) / マンスリー (Monthly)
  - ルーキー (Rookie) / 男子人気 (Male) / 女子人気 (Female)
  - R-18 デイリー / R-18 ウィークリー / R-18G
- **ショートフィード (`ShortsFeedScreen`)**: フルスクリーン縦スワイプによる作品流し読み機能。
- **小説 (`NovelScreen`)**: 小説の検索・シリーズ作品一覧・リーダー画面。

---

## 各ビューアの詳細仕様

### 1. イラスト・マンガビューア (`ImageViewerScreen`)

- **ズーム & 移動**: Pinch-to-zoom、ダブルタップズーム、ドラッグ移動。
- **見開き / 単ページ切り替え**: `mangaReaderMode` (`paged` / `continuous` / `webtoon`)。
- **画質設定パラメータ (`AppSettings`)**:
  画質はネットワーク負荷と表示精細度のバランスに合わせて個別に調整可能です。

| 設定項目名 | 選択値 | 説明 |
| :--- | :--- | :--- |
| `feedPreviewQuality` | `low` / `high` | フィードリスト上のサムネイル画像解像度 |
| `illustDetailQuality` | `medium` / `high` / `original` | イラスト詳細画面でのメイン表示画像解像度 |
| `mangaDetailQuality` | `low` / `high` / `original` | マンガページ閲覧時の解像度 |
| `fullscreenQuality` | `high` / `original` | フルスクリーン拡大ビューアでの解像度 |

---

### 2. うごイラ再生 (`UgoiraArtwork`)

うごイラ（アニメーションイラスト）は、Zip 圧縮された連続フレーム画像とミリ秒単位の遅延配列 (`UgoiraFrame`) で構成されています。

```
 [ Ugoira Metadata Request ] ──> Zip URL & Frame Delay List ([{file: "000000.jpg", delay: 125}, ...])
                                         │
 [ Async Zip Download ] ───────> Memory Unzip & Cache
                                         │
 [ Canvas Re-render Engine ] <── Frame Timing Scheduler (Coroutine Loop)
```

- **非同期解凍・キャッシュ**: Zip アーカイブをダウンローダーで取得し、メモリ上で展開。
- **再生制御**: 再生 / 一時停止、ループ無効化、フレーム単位のステップ送り。
- **エクスポート機能**: APNG / GIF / MP4 フォーマットへの変換保存対応。

---

### 3. 小説リーダー (`NovelScreen`)

- **縦書き / 横書き表示**: 日本語小説に適した縦書きレイアウト表示（右から左へのページ推移）に対応。
- **リーダーカスタマイズ**:
  - `appFont`: システムフォントまたはカスタム明朝/ゴシック体
  - フォントサイズ (sp)、行間倍率、余白幅の動的調整
  - テーマ設定（ホワイト / セピア / ダーク / ブラック）
- **挿絵表示**: 文中に埋め込まれた `[pixivimage:illust_id-page]` タグを自動検出・インライン表示し、タップで拡大ポップアップ表示。

---

## ユーザープロフィール (`UserProfileScreen`)

作者詳細画面では以下のデータが表示・操作可能です。
- 投稿作品タブ（イラスト / マンガ / 小説 / シリーズ）
- 公開ブックマーク作品一覧
- プロファイル情報（自己紹介文、外部 Web / SNS リンク）
- **フォロー管理**: 公開フォロー (`Restrict.Public`) / 非公開フォロー (`Restrict.Private`)
- **ミュート管理**: このユーザーをミュートリスト (`mutedUsers`) に登録
