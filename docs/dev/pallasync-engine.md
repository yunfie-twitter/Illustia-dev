---
title: PallaSync 同期エンジン仕様
description: 設定暗号化、データ同期プロトコル、コンフリクト解決アルゴリズム
---

# PallaSync 同期エンジン仕様

`pallasync` パッケージは、設定データおよび状態を端末間で安全に保護・同期するためのカスタム同期サブシステムです。

---

## 主要クラス構成

- **`PalleriaSyncManager`**: 同期処理の全体ライフサイクルを管轄。
- **`PallaSyncKeystore`**: `AndroidKeyStore` 内で生成した 256 ビット AES 鍵を用い、データの暗号化（AES-256-GCM / NoPadding）および改ざん検証タグ付与を実施。
- **`PallaSyncConflictResolver`**: タイムスタンプに基づく LWW (Last-Write-Wins) アルゴリズムで最終変更優先の競合解決を実行。
- **`PallaSyncEventApplier`**: 解決されたイベントをローカル DataStore および Room DB に同期適用。
