---
title: アーキテクチャ構成 & データフロー
description: Palleria のレイヤー構造、クラス分類、ViewModel / Repository パターン
---

# アーキテクチャ構成 & データフロー

Palleria は、関心事の分離（Separation of Concerns）に基づいたマルチレイヤー構造を採用しています。

---

## 全体コンポーネントマップ

```
  [ UI Layer (Compose Screens) ]
    │   ▲
    │   │  (StateFlow / UiState)
    ▼   │
  [ ViewModel Layer (IllustiaViewModelCore) ]
    │   ▲
    │   │  (Domain Models: Illust, UserProfile, etc.)
    ▼   │
  [ Repository Layer (IllustiaRepository / ManagedDataRepository) ]
    ├───> [ Local Storage (Room DB & DataStore) ]
    │
    └───> [ Native Bridge Layer (com.yunfie.illustia.rust) ]
            │ (JNI Call)
            ▼
          [ Rust Core (pixiv-api) ] ──( HTTP )──> [ Pixiv API Server ]
```

---

## レイヤー別役割定義

### 1. UI レイヤー (`ui/screens/` & `ui/components/`)
- **Jetpack Compose + Miuix KMP**: 宣言型 UI コンポーネントで構成。
- 単一方向データフロー（Unidirectional Data Flow）を遵守し、状態は `StateFlow` 経由で受信、ユーザーアクションは ViewModel のメソッド呼び出しとして送信。

### 2. ViewModel レイヤー (`viewmodel/`)
- **`IllustiaViewModelCore`**: アプリの中心的な状態保持クラス。`UiState` を生成し、画面ごとのローディング、エラー、データ一覧状態を一元管理。
- **`ViewModelSnapshots`**: 画面遷移時のスクロール位置や検索条件のスナップショットを保持。

### 3. リポジトリ & データレイヤー (`data/`)
- **`IllustiaRepository`**: ネットワークデータとローカルキャッシュを仲介するリポジトリ。
- **`ManagedDataRepository`**: 設定情報およびダウンロードキューの永続化状態を管理。
- **`PixivApiClient` / `RustPixivHttpClient`**: Rust ネイティブ層の API インターフェースのラッパー。

### 4. ネイティブブリッジレイヤー (`nativebridge/` / `rust/`)
- UniFFI により生成された Kotlin バインディングコード (`PixivHttpClient`, `ApiException`)。
- Java Native Interface (JNI) を介して、C ABI 互換の共有ライブラリ (`.so`) を安全に実行。

### 5. ストレージレイヤー (`settings/db/`)
- **Room Database (`IllustiaDatabase`)**: ログインアカウント (`AccountEntity`)、履歴、保存済み作品データを保存。
- **DataStore (`SettingsStore`)**: アプリの個人設定項目 (`AppSettings`) をキー・バリュー形式で管理。
