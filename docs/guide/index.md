---
title: Palleria の概要
description: Palleria の概要、特徴、および動作要件
---

# Palleria の概要

**Palleria** は Android 13（API レベル 33）以上を対象とした非公式のオープンソース Pixiv クライアントアプリです。
UI フレームワークに **Jetpack Compose** と **Miuix KMP**、通信・データ解読コアに **Rust (UniFFI)** を採用しています。

::: warning
Palleria は非公式クライアントであり、ピクシブ株式会社とは一切関係ありません。
:::

---

## 主な機能仕様

- **作品閲覧**: イラスト、マンガ（見開き/単ページ）、小説（縦書き/横書き/挿絵表示）、うごイラ（Zip フレーム解凍・再生）、ショートフィード。
- **検索システム**: タグ部分一致/完全一致、タイトル・キャプション検索、ユーザー検索、ブックマーク数フィルター、ウォッチリスト（お気に入りタグ）。
- **ダウンロード管理**: 原寸保存、一括ダウンロード、並列数設定（1〜5）、作者別/作品別フォルダ振り分け、自動ブックマーク連携。
- **セキュリティ・プライバシー**: PIN / 生体認証、電卓偽装モード（計算機能付き偽装画面）、`FLAG_SECURE` によるスクショ防止、バックグラウンド自動ロック。
- **カスタマイズ & 設定**: ライト/ダーク/AMOLED テーマ、動的カラー、プロキシ接続、ユーザー/タグ/作品IDのミュート、JSON 形式の設定バックアップ/復元。
- **システム統合**: 定期切り替えライブ壁紙サービス (`PalleriaLiveWallpaperService`)、ホーム画面ランキング/イラストウィジェット。

---

## 動作要件

| 項目 | 条件 / 仕様 |
| :--- | :--- |
| **最小 OS バージョン** | Android 13 (API Level 33) 以上 |
| **ターゲット SDK** | Android 14 (API Level 34) |
| **アーキテクチャ** | `arm64-v8a`, `armeabi-v7a`, `x86_64` |
| **開発言語** | Kotlin 2.x, Rust (2021 edition) |
| **ライセンス** | GNU General Public License v3.0 (GPL-3.0-only) |

---

## スクリーンショット

| ホーム画面 | 検索画面 | ランキング | ユーザープロフィール |
| :---: | :---: | :---: | :---: |
| <img src="https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/phoneScreenshots/1.png" width="160" alt="Home" /> | <img src="https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/phoneScreenshots/2.png" width="160" alt="Search" /> | <img src="https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/phoneScreenshots/3.png" width="160" alt="Ranking" /> | <img src="https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/phoneScreenshots/5.png" width="160" alt="Profile" /> |
