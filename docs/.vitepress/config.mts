import { defineConfig } from 'vitepress'

export default defineConfig({
  base: '/Palleria/',
  title: "Palleria",
  description: "Android向けのモダンで高速なオープンソースPixivクライアント「Palleria」の公式ドキュメント",
  lang: 'ja-JP',
  cleanUrls: true,
  lastUpdated: true,
  
  head: [
    ['link', { rel: 'icon', href: '/logo.svg' }]
  ],

  themeConfig: {
    logo: '/logo.svg',
    siteTitle: 'Palleria Docs',
    
    nav: [
      { text: 'ガイド', link: '/guide/' },
      { text: '機能一覧', link: '/features/browse' },
      { text: 'アーキテクチャ', link: '/architecture/' },
      { text: '寄付・支援', link: '/donate' },
      { text: 'GitHub', link: 'https://github.com/yunfie-twitter/Palleria' }
    ],

    sidebar: [
      {
        text: 'スタートガイド',
        collapsed: false,
        items: [
          { text: 'Palleriaとは', link: '/guide/' },
          { text: 'インストール', link: '/guide/installation' },
          { text: 'ログインと認証', link: '/guide/authentication' },
          { text: 'ビルド手順', link: '/guide/build' },
          { text: 'プライバシーポリシー', link: '/privacy-policy' },
          { text: '寄付・開発支援', link: '/donate' }
        ]
      },
      {
        text: '機能ガイド',
        collapsed: false,
        items: [
          { text: '作品閲覧 (イラスト・マンガ・小説)', link: '/features/browse' },
          { text: '検索機能 & ウォッチリスト', link: '/features/search' },
          { text: 'ブックマーク & フォロー管理', link: '/features/bookmarks-following' },
          { text: 'ダウンロードマネージャー', link: '/features/downloads' },
          { text: '閲覧 & 検索履歴', link: '/features/history' },
          { text: 'テーマ & 個人設定・バックアップ', link: '/features/personalization' },
          { text: 'プライバシー & セキュリティ保護', link: '/features/privacy-security' },
          { text: 'ライブ壁紙 & ウィジェット', link: '/features/wallpaper-widget' },
          { text: 'テレメトリ & クラッシュレポート', link: '/features/telemetry' },
          { text: 'データ同期 (PallaSync)', link: '/features/sync' }
        ]
      },
      {
        text: 'アーキテクチャ',
        collapsed: false,
        items: [
          { text: '全体構造・UI層', link: '/architecture/' },
          { text: 'Rust Native コア (pixiv-api)', link: '/architecture/rust-core' },
          { text: 'PallaSync 同期エンジン', link: '/architecture/pallasync' }
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
