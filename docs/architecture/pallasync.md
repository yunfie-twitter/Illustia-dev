---
title: PallaSync 同期エンジン仕様
description: 設定暗号化、データ同期プロトコル、コンフリクト解決アルゴリズム
---

# PallaSync 同期エンジン仕様

`pallasync` パッケージは、設定データおよび状態を端末間で安全に保護・同期するためのカスタム同期サブシステムです。

---

## 主要クラス構成

- **`PalleriaSyncManager`**:
  同期処理の全体ライフサイクルを管轄。バックグラウンドイベントキューの管理およびリモートサーバー (`https://api.yunfi.f5.si`) との通信を制御。
- **`PallaSyncKeystore`**:
  `AndroidKeyStore` 内で生成した 256 ビット AES 鍵を用い、データの暗号化（AES-256-GCM / NoPadding）および改ざん検証タグ付与を実施。
- **`PallaSyncConflictResolver`**:
  複数端末間やローカル/リモート間で設定データが衝突（コンフリクト）した際、タイムスタンプに基づく LWW (Last-Write-Wins) アルゴリズムで最終変更優先の競合解決を実行。
- **`PallaSyncEventApplier`**:
  解決されたイベント（ブックマーク追加、テーマ変更、お気に入りタグ更新等）をローカル DataStore および Room DB に同期適用。

---

## 暗号化シーケンス

```
 [ 平文ペイロード ] ──> [ PallaSyncKeystore ]
                              │ (Android KeyStore AES-256-GCM 鍵)
                              ▼
                        [ IV (12 bytes) ] + [ 暗号文 ] + [ GCM Tag (16 bytes) ]
                              │
 [ ネットワーク送信 / JSON ファイル出力 ] <── (Base64 エンコード)
```

1. アプリ起動時に `AndroidKeyStore` から秘密鍵を安全に読み込み。
2. 同期データまたはバックアップ JSON ファイルの書き出し時、ランダムな初期化ベクター (IV) を生成して暗号化。
3. 読み込み時は GCM 認証タグを検証し、改ざんがないことを確認して安全に復号。
