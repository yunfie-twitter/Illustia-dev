---
title: ダウンロードマネージャー & オフラインライブラリ
description: ファイル命名規則、並列キュー制御、フォルダ自動振り分け、オフライン DB スキーマ
---

# ダウンロードマネージャー & オフラインライブラリ

作品のダウンロード保存、キュー管理、およびオフラインライブラリの技術仕様です。

---

## 保存ルール & ファイル命名規則 (`DownloadNaming.kt`)

`AppSettings` の設定に基づいて、保存先のディレクトリ構造とファイル名が動的に生成されます。

### 1. フォルダ分類オプション
- `downloadFolderByArtist`: 有効時、`Pictures/Palleria/{artist_name}_{artist_id}/` 形式で作者別フォルダを作成。
- `downloadFolderByWork`: 有効時、上記の中に `{illust_id}_{title}/` 形式で作品別サブフォルダを作成。

### 2. トークン置換パターン
ファイル名フォーマット設定において利用可能な変数パターンの一覧です。

| トークン | 置換される値 | 例 |
| :--- | :--- | :--- |
| `{illust_id}` | 作品 ID | `12345678` |
| `{title}` | 作品タイトル | `夏空イラスト` |
| `{artist}` | 作者の表示名 | `ゆんふぃ` |
| `{artist_id}` | 作者の Pixiv ID | `987654` |
| `{page}` | ページ番号 (0 始まりまたは 1 始まり) | `p0` |
| `{ext}` | ファイル拡張子 | `jpg` / `png` / `gif` |

---

## ダウンロードキュー制御 (`DownloadQueueScreen`)

ダウンロードタスクはバックグラウンドサービスおよび非同期 Coroutine チャネルで管理されます。

- `simultaneousDownloads`: 同時並行ダウンロード数の上限設定（設定値: `1` 〜 `5`）。
- `offlineWifiOnly`: 有効時、Wi-Fi ネットワーク接続時のみキューの消化を実行。
- **エラーリトライ**: 通信一時断や 5xx エラーが発生した場合、指数バックオフで最大 3 回まで再試行。

---

## オフラインライブラリ Room DB スキーマ

ダウンロード完了した作品メタデータおよびローカルファイルパスは Room データベースで保存管理されます。

### 1. `SavedIllustEntity` (作品エンティティ)
- `illustId` (`Long`, PK): 作品 ID
- `title` (`String`): タイトル
- `userId` (`Long`): 作者 ID
- `userName` (`String`): 作者名
- `savedAt` (`Long`): 保存日時タイムスタンプ
- `pageCount` (`Int`): 総ページ数

### 2. `SavedIllustPageEntity` (ページエンティティ)
- `id` (`Long`, PK Auto): ページ ID
- `illustId` (`Long`, FK): 対応する作品 ID
- `pageIndex` (`Int`): ページインデックス
- `localFilePath` (`String`): 端末内のローカルファイルパス (`file:///...`)
- `originalUrl` (`String`): 元の原寸画像 URL

### 3. `SavedIllustWithPages` (リレーション)
`@Relation` アノテーションにより `SavedIllustEntity` と複数の `SavedIllustPageEntity` を 1 対多で結合取得し、オフライン環境 (`OfflineLibraryScreen`) での完全ローカル閲覧を実現。
