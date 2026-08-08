---
title: 外観 & 個人設定・バックアップ
description: テーマ設定、プロキシ、ミュート管理、JSON バックアップ
---

# 外観 & 個人設定・バックアップ

`AppSettings` データストアに保持される各種カスタマイズ機能の仕様です。

---

## 外観 & テーマ設定 (`GeneralSettingsScreen`)

- `themeMode`: テーマの選択 (`system` / `light` / `dark`)
- `amoledMode`: 背景色に純黒 (`#000000`) を使用する AMOLED モード (Boolean)
- `useDynamicColor`: Android 12+ Material You ダイナミックカラーの適用 (Boolean)
- `seedColor`: ダイナミックカラー無効時のカスタムシードカラー値 (`0xFF42A5F5L` 等)
- `appLanguage`: アプリ表示言語 (`system` / `ja` / `en`)
- `appFont`: フォント設定 (`system` / カスタムフォント)

---

## ネットワーク & プロキシ設定 (`ImageSettingsScreen`)

- `pixivNetworkMode`: ネットワーク接続モード (`standard` / `proxy`)
- `pixivImageProxyBaseUrl`: ドメインブロック等を回避するためのカスタムプロキシサーバー URL

---

## ミュート機能 (`MuteSettingsScreen`)

フィードや検索結果から特定のコンテンツを除外できます。

- `mutedUsers`: 除外するユーザー ID のリスト (`List<Long>`)
- `mutedTags`: 除外するタグ文字列のリスト (`List<String>`)
- `mutedIllusts`: 除外する作品 ID のリスト (`List<Long>`)

---

## データバックアップ & 復元 (`AppDataScreen`)

設定項目、ウォッチリスト、ミュートリストなどを JSON フォーマットでエクスポート / インポートできます。

- **エクスポート**: 設定データをローカルストレージへ `.json` ファイルとして書き出し。
- **インポート**: 外部の `.json` ファイルを読み込み、DataStore および Room データベースに値を反映。
