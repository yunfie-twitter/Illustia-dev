---
title: テレメトリ & クラッシュハンドラー仕様
description: Firebase Crashlytics / Performance の動的切り替えと CrashHandler 実装
---

# テレメトリ & クラッシュハンドラー仕様

---

## SDK 動的制御 (`IllustiaApplication`)

`AppSettings.sendTelemetry`（デフォルト `false`）の値に基づき、`FirebaseCrashlytics` および `FirebasePerformance` の有効化・無効化を動的切り替え。

```kotlin
FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(settings.sendTelemetry)
FirebasePerformance.getInstance().isPerformanceCollectionEnabled = settings.sendTelemetry
```

---

## ローカルクラッシュハンドラー (`CrashHandler`)

`Thread.UncaughtExceptionHandler` を継承し、未捕獲例外発生時にスタックトレースを解析。リソース未検出 (`Resources.NotFoundException`) やドキュメントアクセス権限エラーを検知してユーザーに復旧ダイアログをトースト表示。
