---
title: ログインと認証メカニズム
description: OAuth2 PKCE 認証フロー、トークン管理、暗号化保存、およびマルチアカウント構造
---

# ログインと認証メカニズム

Palleria は Pixiv 公式の OAuth2 認証モデルを採用しています。認証情報は端末内のセキュアな領域に保護・保存されます。

---

## 認証アーキテクチャ

```
 [ ユーザー ] ──( ログイン要求 )──> [ Custom Tabs / WebView ]
                                            │ (Pixiv ログイン画面)
 [ Palleria アプリ ] <──( pixiv:// リダイレクト )──┘
        │
        ├──> [ Rust API クライアント (pixiv-api) ] ──( OAuth2 Exchange )──> [ Pixiv Auth Server ]
        │                                                                           │
        └──<───────────────────( Token / Refresh Token )───────────────────────────┘
        │
        └──> [ KeyStore (AES-256-GCM) ] ──> Room DB (AccountEntity)
```

---

## ログイン方式

### 1. Web ログイン (標準)

ブラウザコンポーネント（Custom Tabs または WebView）を経由して公式ログイン画面を呼び出し、認証コードを取得します。

- **リダイレクト URI**: `pixiv://account/login`
- **PKCE (Proof Key for Code Exchange)**: 認証リクエスト時にコードチャレンジャーを動的生成し、トークン交換時の安全性を確保。
- **サードパーティ連携**: Google, Apple, X (旧 Twitter) 経由でのシングルサインオンにも対応。

---

## 2. リフレッシュトークンログイン

所持している Pixiv の `refresh_token` 文字列を直接入力してログインします。

1. 入力されたトークン文字列を受け取り、`RustPixivHttpClient` を通じて Pixiv 認証エンドポイント (`https://oauth.secure.pixiv.net/auth/token`) へリクエストを送信。
2. レスポンスとして新しい `access_token` と `refresh_token` を取得・検証後に保存。

::: warning
`refresh_token` はアカウント操作が可能な機密データです。第三者への共有やログへの記録にご注意ください。
:::

---

## トークンの安全な保存と自動更新

- **暗号化保存**:
  取得された認証トークンは `EncryptedSharedPreferences` および `PallaSyncKeystore` を通じて `AndroidKeyStore` 内の AES-256-GCM マスターキーで暗号化され保存されます。
- **トークン自動更新 (Refresh Token Rotation)**:
  `access_token` の有効期限切れ（HTTP 400/401 エラー検知時）を検知すると、バックグラウンドで自動的に `refresh_token` を使用して新しいアクセストークンを再発行します。

---

## マルチアカウント管理構造

複数の Pixiv アカウントをアプリ内に保存・保持できます。

### データベース設計 (`AccountEntity`)
Room データベースの `AccountEntity` テーブルでアカウント状態を管理します。

| カラム名 | 型 | 説明 |
| :--- | :--- | :--- |
| `userId` | `Long` (PK) | Pixiv ユーザー ID |
| `name` | `String` | ユーザー表示名 |
| `account` | `String` | アカウント ID (ログイン ID) |
| `profileImage` | `String` | アイコン画像 URL |
| `refreshToken` | `String` | 暗号化保存されたリフレッシュトークン |
| `isPremium` | `Boolean` | プレミアム会員フラグ |
| `lastActiveAt` | `Long` | 最終使用タイムスタンプ |

### アカウント切り替え
- アカウント切り替えシート (`AccountSwitchSheet`) からワンタップでアクティブアカウント (`activeAccountIndex`) を変更可能。
- 各アカウントの個別ログアウト、トークン再取得、プロファイル再同期をサポート。
