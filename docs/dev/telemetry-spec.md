---
title: テレメトリ & クラッシュハンドラー仕様
description: GlitchTip の動的切り替えと CrashHandler 実装
---

# テレメトリ & クラッシュハンドラー仕様

---

## SDK 動的制御 (`IllustiaApplication`)

`AppSettings.sendTelemetry`（デフォルト `false`）の値に基づき、GlitchTip 互換の Sentry Android SDK を動的に開始・停止します。

SDK の ContentProvider による自動初期化は `AndroidManifest.xml` の `io.sentry.auto-init=false` で無効化されています。ユーザーが明示的にオプトインした場合に限り `GlitchTipTelemetry` が SDK を初期化し、オプトアウト時は直ちに停止します。

```kotlin
GlitchTipTelemetry.setEnabled(applicationContext, settings.sendTelemetry)
```

GlitchTip の DSN は `AndroidManifest.xml` に設定し、パフォーマンストレースは 1% をサンプリングします。GlitchTip が非対応の自動セッション追跡、既定 PII、ネットワークイベントおよびユーザー操作の breadcrumb は無効です。

---

## ローカルクラッシュハンドラー (`CrashHandler`)

`Thread.UncaughtExceptionHandler` を継承し、未捕獲例外発生時にスタックトレースを解析。リソース未検出 (`Resources.NotFoundException`) やドキュメントアクセス権限エラーを検知してユーザーに復旧ダイアログをトースト表示。
