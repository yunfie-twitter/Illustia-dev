---
title: テレメトリ & クラッシュレポート
description: テレメトリ収集 (Firebase Crashlytics / Performance Monitoring) の仕組みとプライバシー設計
---

# テレメトリ & クラッシュレポート

Palleria は、アプリの安定性向上およびパフォーマンス改善のためにテレメトリ収集（ログ送信）機能を搭載しています。ただし、プライバシー尊重のためデフォルトでは無効化（オプトイン方式）されています。

---

## 収集メカニズム

テレメトリ機能が有効化された場合、以下の 2 つのサービスが動作します。

| サービス | クラス / SDK | 目的・収集内容 |
| :--- | :--- | :--- |
| **Firebase Crashlytics** | `FirebaseCrashlytics` | 未捕獲のクラッシュログ、例外スタックトレース、OS バージョン、端末モデル |
| **Firebase Performance** | `FirebasePerformance` | アプリ起動時間、ネットワークリクエストの応答時間（レスポンスタイム） |

---

## ソースコード実装仕様

### 1. オプトイン設定 (`AppSettings.sendTelemetry`)

- `AppSettings.sendTelemetry`: データストア項目 (`sendTelemetry`, デフォルト値 `false`)。
- 「設定」>「データ設定」画面 (`DataSettingsScreen`) の「テレメトリデータを送信」スイッチからいつでも変更可能です。

### 2. 動的な有効化 / 無効化制御 (`IllustiaApplication`)

アプリ起動時（`IllustiaApplication.onCreate`）および設定変更時に、設定値に応じて動的に SDK の有効・無効を切り替えます。

```kotlin
// settings.sendTelemetry が false の場合、送信処理は一切行われません
FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(settings.sendTelemetry)
FirebasePerformance.getInstance().isPerformanceCollectionEnabled = settings.sendTelemetry
```

### 3. ローカルクラッシュハンドラー (`CrashHandler`)

外部サービスへの送信有無にかかわらず、アプリ内では `CrashHandler`（`Thread.UncaughtExceptionHandler`）が未補獲の例外を捕捉します。

- **リソースエラー判定**: リソース読み込み失敗 (`Resources.NotFoundException` / `InflateException`) の特定。
- **ストレージ権限指示**: 保存先ストレージのアクセス権限エラー時、適切な復旧案内を表示。
- **キャンセルの安全処理**: Coroutine のキャンセル例外 (`CancellationException`) はクラッシュ扱いから除外。

---

## プライバシー & データセキュリティ

- **完全オプトイン**: 初期状態で送信が無効になっているため、同意なしにデータが外部へ送信されることはありません。
- **個人情報の非収集**: Pixiv アカウントのログイン情報（トークン、パスワード）、検索履歴、閲覧履歴、保存された画像データがテレメトリに含まれることはありません。
