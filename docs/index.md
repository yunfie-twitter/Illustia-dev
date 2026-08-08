---
layout: home

hero:
  name: "Palleria"
  text: "Pixiv client for Android"
  tagline: "Jetpack Compose と Rust ネイティブエンジンで構築された Android 向け Pixiv クライアント"
  image:
    src: /logo.svg
    alt: Palleria Icon
  actions:
    - theme: brand
      text: ユーザーガイド
      link: /user/
    - theme: alt
      text: 開発者ドキュメント
      link: /dev/

features:
  - title: 快適な作品鑑賞
    details: イラスト、マンガ、小説（縦書き対応）、うごイラ（Zip解凍再生）、ショートフィードに対応。
  - title: 簡単操作と安心セキュリティ
    details: タグ検索、ウォッチリスト機能に加え、PINロックや本物として使える電卓偽装モードを搭載。
  - title: 高速な Rust コア
    details: 通信・JSON解析を Rust ネイティブクレートで処理し、大容量データでもメモリ消費を強力に抑制。
  - title: 高機能ダウンロード & 壁紙
    details: 原寸画像の保存、作者別自動フォルダ分け、お気に入り作品の自動切り替えライブ壁紙サービス。
---

<style>
:root {
  --vp-home-hero-name-color: transparent;
  --vp-home-hero-name-background: -webkit-linear-gradient(120deg, #7f52ff 30%, #ff6900);
}
</style>
