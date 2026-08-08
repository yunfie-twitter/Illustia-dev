---
title: テーマ & 個人設定・バックアップ
description: DataStore 設定パラメータ一覧、プロキシ接続、ミュート制御、JSON バックアップ
---

# テーマ & 個人設定・バックアップ

`AppSettings` データストアに保持される各種カスタマイズ項目の仕様です。

---

## 設定パラメータ一覧 (`AppSettings` DataStore)

### 1. 外観 & テーマ (`GeneralSettingsScreen`)

| フィールド名 | 型 | デフォルト値 | 説明 |
| :--- | :--- | :--- | :--- |
| `themeMode` | `String` | `"system"` | テーマ選択 (`system` / `light` / `dark`) |
| `amoledMode` | `Boolean` | `false` | 有機EL向け純黒背景 (`#000000`) モード |
| `useDynamicColor` | `Boolean` | `true` | Android 12+ Material You ダイナミックカラー適用 |
| `seedColor` | `Long` | `0xFF42A5F5L` | カスタムテーマシードカラー |
| `verticalColumnCount` | `Int` | `2` | 縦画面（ポートレート）時のグリッド列数 |
| `horizontalColumnCount` | `Int` | `4` | 横画面（ランドスケープ）時のグリッド列数 |
| `appLanguage` | `String` | `"system"` | アプリ言語設定 (`system` / `ja` / `en`) |
| `appFont` | `String` | `"system"` | アプリフォント設定 |
| `hapticMode` | `String` | `"rich"` | 触覚フィードバック設定 (`off` / `light` / `rich`) |

---

### 2. ネットワーク & プロキシ (`ImageSettingsScreen`)

| フィールド名 | 型 | デフォルト値 | 説明 |
| :--- | :--- | :--- | :--- |
| `pixivNetworkMode` | `String` | `"standard"` | 接続モード (`standard` / `proxy`) |
| `pixivImageProxyBaseUrl` | `String` | `""` | カスタムプロキシサーバーのベース URL |
| `imageCacheSizeMb` | `Int` | `300` | 画像ディスクキャッシュの上限サイズ (MB) |
| `smartCacheEnabled` | `Boolean` | `false` | スマート事前キャッシュの有効化 |

---

## ミュート機能 (`MuteSettingsScreen`)

指定した条件に合致する作品をフィードや検索結果から全自動でフィルタリング除外します。

- `mutedUsers`: 除外対象のユーザー ID リスト (`List<Long>`)
- `mutedTags`: 除外対象のタグ文字列リスト (`List<String>`)
- `mutedIllusts`: 除外対象の単体作品 ID リスト (`List<Long>`)

---

## JSON データバックアップ & 復元 (`AppDataScreen`)

アプリ内の設定情報、ウォッチリスト、ミュート条件などを構造化 JSON データとしてローカルストレージへ保存・復元できます。

### エクスポートデータ構造例
```json
{
  "version": 1,
  "exportedAt": 1723123456789,
  "settings": {
    "themeMode": "dark",
    "amoledMode": true,
    "allowR18": false,
    "simultaneousDownloads": 3
  },
  "favoriteTags": ["原神", "東方Project"],
  "mutedUsers": [12345, 67890],
  "mutedTags": ["ネタバレ", "R-18G"],
  "mutedIllusts": []
}
```

- **バックアップ作成**: JSON ファイルとして端末のダウンロード・ドキュメントフォルダへ保存。
- **復元**: 選択した JSON ファイルを検証し、`AppSettings` および `FavoriteTagEntity` テーブルへ復元。
