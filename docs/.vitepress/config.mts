import { defineConfig } from 'vitepress'

export default defineConfig({
  base: '/Palleria/',
  title: "Palleria",
  titleTemplate: ':title | Palleria - Android Pixiv Client',
  description: "Android向け高速オープンソースPixivクライアント「Palleria」の公式ドキュメント。イラスト、マンガ、小説の閲覧、うごイラ再生、暗号化同期、プライバシー保護、電卓偽装モードをサポート。",
  lang: 'ja-JP',
  cleanUrls: true,
  lastUpdated: true,

  // SEO 用 sitemap.xml 自動生成
  sitemap: {
    hostname: 'https://yunfi.f5.si/Palleria/'
  },

  head: [
    ['link', { rel: 'icon', href: '/logo.svg', type: 'image/svg+xml' }],
    ['meta', { name: 'keywords', content: 'Palleria, Pixiv, Android, Pixivクライアント, オープンソース, Jetpack Compose, Rust, うごイラ, マンガ, 小説, 縦書き, アプリロック, 電卓偽装, F-Droid' }],
    ['meta', { name: 'author', content: 'ゆんふぃ (yunfie)' }],
    ['meta', { name: 'robots', content: 'index, follow' }],
    
    // OGP メタタグ
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:site_name', content: 'Palleria Official Documentation' }],
    ['meta', { property: 'og:title', content: 'Palleria - Fast & Modern Pixiv Client for Android' }],
    ['meta', { property: 'og:description', content: 'Android向け高速オープンソースPixivクライアント「Palleria」の公式ドキュメント。作品閲覧、ダウンロード、プライバシー保護機能を網羅。' }],
    ['meta', { property: 'og:image', content: 'https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/icon.png' }],
    ['meta', { property: 'og:url', content: 'https://yunfi.f5.si/Palleria/' }],
    
    // Twitter Cards
    ['meta', { name: 'twitter:card', content: 'summary_large_image' }],
    ['meta', { name: 'twitter:title', content: 'Palleria Docs - Android Pixiv Client' }],
    ['meta', { name: 'twitter:description', content: 'Jetpack ComposeとRustで構築されたAndroid向けオープンソースPixivクライアント Palleria の公式ガイド' }],
    ['meta', { name: 'twitter:image', content: 'https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/icon.png' }]
  ],

  themeConfig: {
    logo: '/logo.svg',
    siteTitle: 'Palleria Docs',

    // ページ内目次 (右側のアウトライン) を全ページで自動表示
    outline: {
      level: [2, 3],
      label: '目次'
    },
    
    nav: [
      { text: 'ユーザーガイド', link: '/user/' },
      { text: '開発者ドキュメント', link: '/dev/' },
      { text: '寄付・支援', link: '/donate' },
      { text: 'GitHub', link: 'https://github.com/yunfie-twitter/Palleria' }
    ],

    // 分割せず一元化した完全サイドバー目次
    sidebar: [
      {
        text: 'ユーザーガイド (使い方)',
        collapsed: false,
        items: [
          { text: 'Palleria とは', link: '/user/' },
          { text: 'アプリの入手とインストール', link: '/user/installation' },
          { text: 'ログイン・アカウント設定', link: '/user/authentication' },
          { text: '作品を見る (イラスト・マンガ・小説)', link: '/user/browse' },
          { text: '検索とお気に入りタグ', link: '/user/search' },
          { text: 'ブックマークとフォロー', link: '/user/bookmarks' },
          { text: '画像の保存と一括ダウンロード', link: '/user/downloads' },
          { text: '外観テーマと各種設定', link: '/user/personalization' },
          { text: 'アプリロックと電卓偽装', link: '/user/security' },
          { text: '端末間データ同期 (PallaSync)', link: '/user/sync' },
          { text: 'ライブ壁紙とウィジェット', link: '/user/wallpaper-widget' }
        ]
      },
      {
        text: '開発者ガイド (技術仕様)',
        collapsed: false,
        items: [
          { text: 'アーキテクチャ概要 & レイヤー設計', link: '/dev/' },
          { text: 'ビルド & 環境構築パイプライン', link: '/dev/build' },
          { text: 'Rust Native コア (pixiv-api)', link: '/dev/rust-core' },
          { text: 'PallaSync 同期エンジン仕様', link: '/dev/pallasync-engine' },
          { text: 'セキュリティ & 電卓パーサー仕様', link: '/dev/security-engine' },
          { text: 'データベース & DataStore スキーマ', link: '/dev/database-schema' },
          { text: 'テレメトリ & クラッシュハンドラー仕様', link: '/dev/telemetry-spec' }
        ]
      },
      {
        text: 'ポリシー & 支援',
        collapsed: false,
        items: [
          { text: 'プライバシーポリシー', link: '/privacy-policy' },
          { text: '寄付・開発支援', link: '/donate' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/yunfie-twitter/Palleria' }
    ],

    footer: {
      message: 'Released under the GPL-3.0-only License.',
      copyright: 'Copyright © 2026 ゆんふぃ (yunfie)'
    },

    search: {
      provider: 'local'
    }
  }
})
