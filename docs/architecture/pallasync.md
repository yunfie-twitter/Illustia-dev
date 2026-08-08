---
title: PallaSync 同期エンジン
description: 設定暗号化、データ同期、コンフリクト解決の仕様
---

# PallaSync 同期エンジン

`pallasync` パッケージは、設定データおよび状態の同期・保護を行うサブシステムです。

---

## 構成モジュール

- **`PalleriaSyncManager`**: バックグラウンドでの同期処理およびイベント管理を行うマネージャー。
- **`PallaSyncKeystore`**: Android KeyStore を使用して AES-256-GCM 鍵を生成・保持し、ローカル保存データや通信用ペイロードを暗号化。
- **`PallaSyncConflictResolver`**: ローカルとリモート間でデータの不整合が生じた際の解決ロジック（LWW: Last-Write-Wins ベース）。
- **`PallaSyncEventApplier`**: 受信した同期イベント（設定変更、お気に入り更新など）を DataStore / Room DB へ適用。

---

## データ暗号化フロー

1. アプリ起動時に `PallaSyncKeystore` が `AndroidKeyStore` から秘密鍵を取得（存在しない場合は自動生成）。
2. エクスポート時および同期通信時、ペイロードデータを AES-GCM 方式で暗号化。
3. 復号処理は自アプリの署名キーとペアリングされた端末環境でのみ実行可能。
