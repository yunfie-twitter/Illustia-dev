---
title: 検索機能 & ウォッチリスト仕様
description: タグ検索、各種フィルター、ソート、お気に入りタグ管理（ウォッチリスト）
---

# 検索機能 & ウォッチリスト仕様

検索画面 (`SearchScreen`) では、複雑な検索クエリの構築とフィルタリング、およびウォッチリスト管理機能を提供します。

---

## 検索モード (`SearchTarget`)

| 識別子 | UI 表示名 | 説明 |
| :--- | :--- | :--- |
| `PartialTags` | **タグ部分一致** | 入力したタグを一部に含む作品を検索 |
| `ExactTag` | **タグ完全一致** | 指定したタグが完全一致する作品のみを検索 |
| `TitleAndCaption` | **タイトル・キャプション** | 作品のタイトルおよび説明文本文を対象にしたテキスト検索 |
| `Users` | **ユーザー検索** | クリエイターの表示名または Pixiv ID によるユーザー検索 |

---

## 検索フィルター & ソート設定

### 1. ソート順 (`SearchSort`)
- `DateDesc`: 投稿日時が新しい順 (降順)
- `DateAsc`: 投稿日時が古い順 (昇順)
- `PopularDesc`: 人気順 (Pixiv Premium または条件適合時)

### 2. 検索対象期間 (`SearchDuration`)
- `All`: 全期間
- `WithinDay`: 24時間以内
- `WithinWeek`: 1週間以内
- `WithinMonth`: 1ヶ月以内

### 3. ブックマーク数フィルター (`SearchBookmarkFilter`)
評価数タグ（〇〇users入り）に適合する作品のみに絞り込みます。
- `None`: 指定なし
- `Bookmark100`: 100users入り 以上
- `Bookmark500`: 500users入り 以上
- `Bookmark1000`: 1000users入り 以上
- `Bookmark5000`: 5000users入り 以上
- `Bookmark10000`: 10000users入り 以上

### 4. コンテンツ・年齢制限 (`allowR18`)
- 全年齢対象 / R-18 / R-18G 作品の表示切り替え。

---

## ウォッチリスト (`FavoriteTagsScreen` / `WatchlistStore`)

頻繁にアクセスするタグを「ウォッチリスト（お気に入りタグ）」としてローカル登録できます。

```
 [ 検索結果画面 ] ──( 「★ ウォッチリストに追加」 )──> Room DB (FavoriteTagEntity)
                                                            │
 [ FavoriteTagsScreen ] <──( 未読件数チェック API 呼び出し )─┘
```

- **データベース構成 (`FavoriteTagEntity`)**:
  - `name`: タグ名 (PK)
  - `translatedName`: 翻訳タグ名
  - `addedAt`: 追加日時タイムスタンプ
  - `lastCheckedAt`: 最終確認日時
- **新着更新チェック**: 各タグの最新作品IDを比較し、新着投稿がある場合に未読バッジを表示。
- **クイック検索**: タグをタップすることで、設定済みのフィルター条件を適用した検索結果を即座に開きます。
