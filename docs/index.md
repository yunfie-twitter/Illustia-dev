---
layout: home

hero:
  name: "Palleria"
  text: "Pixiv client for Android"
  tagline: "Jetpack Compose と Rust ネイティブエンジンで構築された Android 向け Pixiv クライアント"
  image:
    src: https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/icon.png
    alt: Palleria Icon
  actions:
    - theme: brand
      text: ドキュメントを読む
      link: /guide/
    - theme: alt
      text: GitHub リポジトリ
      link: https://github.com/yunfie-twitter/Palleria

features:
  - title: Compose & Miuix UI
    details: Jetpack Compose および Miuix KMP コンポーネントによる UI 構成。ダークモード・AMOLED テーマに対応。
  - title: Rust Native コア
    details: 通信処理および API JSON のパースを Rust クレート (`pixiv-api`) で処理し、メモリ消費とクラッシュを防止。
  - title: 多種メディア対応
    details: イラスト、マンガ、小説（縦書き対応）、うごイラ（アニメーション Zip 再生）、ショートフィードの閲覧機能。
  - title: アプリ偽装 & セキュリティ
    details: PIN / 生体認証、電卓偽装モード、タスク一覧での画面ぼかし、FLAG_SECURE によるスクリーンショット防止。
  - title: 高機能ダウンロード
    details: 原寸画像の保存、作者別/作品別フォルダ自動分類、マルチスレッド並列キュー管理、自動ブックマーク連携。
  - title: ライブ壁紙 & ウィジェット
    details: ブックマーク作品のローテーション表示（ライブ壁紙）およびホーム画面用ランキング表示ウィジェット。
---

<style>
:root {
  --vp-home-hero-name-color: transparent;
  --vp-home-hero-name-background: -webkit-linear-gradient(120deg, #7f52ff 30%, #ff6900);
}
</style>
