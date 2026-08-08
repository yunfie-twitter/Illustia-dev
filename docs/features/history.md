---
title: 閲覧 & 検索履歴仕様
description: 閲覧履歴、検索履歴の Room DB スキーマ、削除処理、シークレットモード
---

# 閲覧 & 検索履歴仕様

閲覧および検索アクティビティの記録・保持・管理機能についての技術仕様です。

---

## 閲覧履歴 (`ViewHistoryScreen`)

過去に表示した作品（イラスト・マンガ・小説）の情報がローカル Room データベースに記録されます。

### データベース構造 (`ViewHistoryEntity`)

| カラム名 | 型 | 説明 |
| :--- | :--- | :--- |
| `illustId` | `Long` (PK) | 作品 ID |
| `title` | `String` | 作品タイトル |
| `userId` | `Long` | 作者の Pixiv ID |
| `userName` | `String` | 作者の表示名 |
| `thumbnailUrl` | `String` | プレビューサムネイル画像 URL |
| `workType` | `String` | 作品タイプ (`illust` / `manga` / `novel`) |
| `pageCount` | `Int` | ページ数 |
| `viewedAt` | `Long` | 最終閲覧日時タイムスタンプ |

### 機能仕様
- **履歴内検索**: 記録されたタイトルや作者名に対するリアルタイムフィルタリング。
- **グループ表示**: 閲覧日時に基づくグループ分け表示（「今日」「昨日」「今週」「以前」）。
- **個別削除 / 一括削除**: 選択項目の個別消去、または全閲覧履歴のデータクリア。

---

## 検索履歴 (`SearchHistoryEntity`)

検索バーに入力されたキーワードおよび検索タグの履歴管理です。

### データベース構造 (`SearchHistoryEntity`)
- `query` (`String`, PK): 検索キーワード・タグ文字列
- `searchedAt` (`Long`): 最終検索日時タイムスタンプ

### 機能仕様
- 検索フォーム入力時のオートコンプリート（検索候補）表示。
- 各候補の末尾アイコンタップによる個別履歴の削除。

---

## 記録制御 & シークレットモード (`AppSettings`)

設定画面 (`DataSettingsScreen`) から記録の保存動作を変更できます。

- `saveViewHistory` (`Boolean`): 閲覧履歴の自動記録設定 (デフォルト `true`)。
- `saveSearchHistory` (`Boolean`): 検索履歴の自動記録設定 (デフォルト `true`)。

::: info プライバシーの保護
両方のフラグを `false` に設定することで、一切のアクティビティログをデータベースへ書き込まないシークレットモードとして動作させることができます。
:::
