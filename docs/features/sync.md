---
title: PallaSync データ同期 & 同期サーバー
description: 設定・状態の端末間同期機能、暗号化アーキテクチャ、同期サーバー仕様
---

# PallaSync データ同期 & 同期サーバー

PallaSync は、複数端末間でアプリの設定、お気に入りタグ（ウォッチリスト）、およびアプリ状態を安全にバックアップ・同期するための機能です。

---

## 主な特徴

- **エンドツーエンド類似の安全な暗号化**: データは送信前に端末上で AES-256-GCM 暗号化されるため、サーバー側で平文データが閲覧・解読されることはありません。
- **バックグラウンド同期**: アプリ起動時およびバックグラウンドで自動的に変更イベントを検知・同期。
- **自動競合解決 (Conflict Resolution)**: 複数デバイスで同時に変更が行われた場合、タイムスタンプに基づく LWW (Last-Write-Wins) ロジックで競合を自動処理。

---

## システム構成 & データフロー

```
 [ Palleria アプリ (端末 A) ]
        │
        ├── 1. 変更イベント発生 (設定 / お気に入りタグ等)
        ├── 2. Android KeyStore で AES-256-GCM 暗号化 (PallaSyncKeystore)
        │
        ▼ 3. HTTPS 通信 (Encrypted Payload)
 [ PallaSync 同期サーバー (https://api.yunfi.f5.si) ]
        ▲
        │ 4. 暗号化データの同期取得
        │
 [ Palleria アプリ (端末 B) ]
        ├── 5. ローカル KeyStore 鍵で復号
        └── 6. LWW 競合解決 (PallaSyncConflictResolver) ──> DataStore / Room DB へ反映
```

---

## 仕様パラメータ & 設定項目 (`AppSettings`)

| 設定キー | 型 | デフォルト値 | 説明 |
| :--- | :--- | :--- | :--- |
| `pallaSyncEnabled` | `Boolean` | `false` | PallaSync 同期機能の有効化 |
| `pallaSyncServerUrl` | `String` | `"https://api.yunfi.f5.si"` | 使用する PallaSync 同期サーバーの Endpoint URL |

::: tip セルフホスト対応
デフォルトでは公式同期サーバー (`https://api.yunfi.f5.si`) を使用しますが、独自に立ち上げた PallaSync 互換サーバーの URL を指定してセルフホスト運用することも可能です。
:::

---

## 内部コンポーネント詳細

### 1. `PalleriaSyncManager`
アプリケーションプロセス内で単一インスタンスとして管理される同期コーディネーターです。
- `startBackgroundSync()`: バックグラウンドでの定期同期ジョブを起動。
- `recoverInterruptedActivation()`: ネットワーク一時断などで中断された同期アクティベーションの自動復旧。

### 2. `PallaSyncKeystore`
暗号化鍵のライフサイクルを管理します。
- `AndroidKeyStore` から 256 ビット AES 鍵を取得/生成。
- データごとにユニークな 12 バイト初期化ベクター (IV) と 16 バイト GCM 認証タグを生成して改ざん防止。

### 3. `PallaSyncConflictResolver`
ローカルとリモートで同一項目の変更が競合した場合、各イベントに付与されたミリ秒精度タイムスタンプを比較し、最も新しい変更（Last-Write-Wins）を安全に採用します。

### 4. `PallaSyncEventApplier`
同期されたイベント（`PallaSyncSettingsEvents`）を読み込み、ローカルの `DataStore`（設定）および Room DB（`FavoriteTagEntity` 等）へ非同期で反映させます。
