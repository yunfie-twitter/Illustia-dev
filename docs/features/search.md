---
title: 検索機能 & ウォッチリスト
description: タグ検索、各種フィルター、ソート、お気に入りタグ管理（ウォッチリスト）
---

# 検索機能 & ウォッチリスト

検索画面 (`SearchScreen`) では、多様なパラメータ指定による検索とウォッチリスト管理が行えます。

---

## 検索ターゲット (`SearchTarget`)

| モード | 指定型 | 説明 |
| :--- | :--- | :--- |
| **タグ部分一致** | `PartialTags` | 指定したタグを含む作品を検索 |
| **タグ完全一致** | `ExactTag` | タグが完全一致する作品のみ検索 |
| **タイトル・キャプション** | `TitleAndCaption` | 作品タイトルおよび説明文を対象としたテキスト検索 |
| **ユーザー検索** | `Users` | ユーザー名および Pixiv ID でのユーザー検索 |

---

## 絞り込み & ソートパラメータ

### ソート条件 (`SearchSort`)
- `DateDesc`: 投稿日時が新しい順
- `DateAsc`: 投稿日時が古い順
- `PopularDesc`: 人気順（Pixiv Premium アカウントまたは条件適合時）

### 期間指定 (`SearchDuration`)
- `All`: 全期間
- `WithinDay`: 24時間以内
- `WithinWeek`: 1週間以内
- `WithinMonth`: 1ヶ月以内

### ブックマーク数フィルター (`SearchBookmarkFilter`)
- `None`: 指定なし
- `Bookmark100`: 100users 入り以上
- `Bookmark500`: 500users 入り以上
- `Bookmark1000`: 1000users 入り以上
- `Bookmark5000`: 5000users 入り以上
- `Bookmark10000`: 10000users 入り以上

---

## ウォッチリスト (`FavoriteTagsScreen` / `WatchlistStore`)

よく使用するタグをウォッチリスト（お気に入りタグ）に登録できます。

- 登録データは `FavoriteTagEntity` テーブルに保存。
- ウォッチリスト画面から各タグの未読・新着作品数を取得し、1 タップで該当タグの検索結果を呼び出し可能。
- タグごとの並び替えおよび個別削除に対応。
