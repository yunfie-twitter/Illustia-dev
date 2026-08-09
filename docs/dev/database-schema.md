---
title: データベース & DataStore スキーマ
description: Room DB エンティティリレーション、AppSettings DataStore フィールド仕様
---

# データベース & DataStore スキーマ

---

## Room データベース (`IllustiaDatabase`)

- **`AccountEntity`**: ログインアカウント情報 (`userId`, `name`, `refreshToken`, `isPremium` 等)
- **`SavedIllustEntity` / `SavedIllustPageEntity` / `SavedIllustWithPages`**: 保存済み作品およびページインデックス、ローカルファイルパス (`SavedIllustWithPages` で 1対多 リレーション取得)
- **`ViewHistoryEntity`**: 閲覧履歴 (`illustId`, `title`, `userId`, `viewedAt` 等)
- **`SearchHistoryEntity`**: 検索履歴 (`query`, `searchedAt`)
- **`FavoriteTagEntity`**: ウォッチリストタグ (`name`, `addedAt`, `lastCheckedAt`)

---

## DataStore (`AppSettings`)

- `themeMode`, `amoledMode`, `useDynamicColor`, `seedColor`, `verticalColumnCount`, `horizontalColumnCount`
- `feedPreviewQuality`, `illustDetailQuality`, `mangaDetailQuality`, `fullscreenQuality`
- `simultaneousDownloads`, `downloadFolderByArtist`, `downloadFolderByWork`, `autoDownloadOnBookmark`
- `appLockEnabled`, `biometricEnabled`, `privacyModeAutoLockTiming`, `secureWindow`, `hideRecents`, `dummyAppName`, `dummyIconVariant`
- `mutedUsers`, `mutedTags`, `mutedIllusts`
- `pallaSyncEnabled`, `pallaSyncServerUrl`
- `sendTelemetry`
