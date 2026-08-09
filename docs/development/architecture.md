---
title: アーキテクチャ
description: PalleriaのAndroid・Rustコードの構成。
---

# アーキテクチャ

## ディレクトリ構成

```text
Palleria/
├─ app/                         Androidアプリ
│  └─ src/main/java/com/yunfie/illustia/
│     ├─ ui/                    Compose UIと画面
│     ├─ data/                  API・リポジトリ・永続化
│     ├─ models/                アプリとAPIのモデル
│     ├─ pallasync/             同期機能のAndroid側
│     ├─ settings/              設定
│     ├─ wallpaper/             壁紙機能
│     └─ widget/                ホーム画面ウィジェット
├─ rust/
│  ├─ pixiv-api/                Pixiv APIとUniFFI公開層
│  └─ pallasync-core/           同期コア
├─ fdroid/                      F-Droidメタデータ
└─ docs/                        VitePressドキュメント
```

## UI

画面はJetpack Composeで実装し、デザインシステムにはMiuix KMPを使用します。新しい画面やコンポーネントは、既存のMiuixテーマ、ナビゲーション、ポップアップ管理のパターンに合わせてください。

## データフロー

UIはViewModelから状態を受け取り、Repositoryとデータ層を通じてネットワーク、データベース、設定へアクセスします。長時間処理はコルーチンで実行し、画面のライフサイクルに合わせてキャンセルできる構成を保ちます。

## Rust連携

`rust/pixiv-api`はRustライブラリをビルドし、UniFFI経由でKotlinバインディングを生成します。Gradleの`preBuild`は、ホスト向けバインディング生成とAndroid ABI向けライブラリビルドに依存します。

対象ABI:

- `arm64-v8a`
- `armeabi-v7a`
- `x86_64`

## Android統合

アプリはURL共有、テキスト共有、ウィジェット、ライブ壁紙、Androidアカウント同期などのプラットフォーム機能を含みます。Manifestや公開コンポーネントを変更するときは、権限と`exported`設定を慎重に確認してください。
