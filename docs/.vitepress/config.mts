import { defineConfig } from 'vitepress'

export default defineConfig({
  base: '/Palleria/',
  title: "Palleria",
  description: "Android 向け Pixiv クライアント「Palleria」の公式ドキュメント",
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
      { text: 'ユーザーガイド', link: '/user/' },
      { text: '開発者ドキュメント', link: '/dev/' },
      { text: '寄付・支援', link: '/donate' },
      { text: 'GitHub', link: 'https://github.com/yunfie-twitter/Palleria' }
    ],

    sidebar: {
      '/user/': [
        {
          text: 'ユーザーガイド (使い方)',
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
            { text: 'ライブ壁紙とウィジェット', link: '/user/wallpaper-widget' },
            { text: 'プライバシーポリシー', link: '/privacy-policy' },
            { text: '寄付・開発支援', link: '/donate' }
          ]
        }
      ],
      '/dev/': [
        {
          text: '開発者ガイド (技術仕様)',
          items: [
            { text: 'アーキテクチャ概要 & レイヤー設計', link: '/dev/' },
            { text: 'ビルド & 環境構築パイプライン', link: '/dev/build' },
            { text: 'Rust Native コア (pixiv-api)', link: '/dev/rust-core' },
            { text: 'PallaSync 同期エンジン仕様', link: '/dev/pallasync-engine' },
            { text: 'セキュリティ & 電卓パーサー仕様', link: '/dev/security-engine' },
            { text: 'データベース & DataStore スキーマ', link: '/dev/database-schema' },
            { text: 'テレメトリ & クラッシュハンドラー仕様', link: '/dev/telemetry-spec' }
          ]
        }
      ],
      // デフォルトフォールバック
      '/': [
        {
          text: 'スタートガイド',
          items: [
            { text: 'ユーザーガイド', link: '/user/' },
            { text: '開発者ドキュメント', link: '/dev/' },
            { text: 'プライバシーポリシー', link: '/privacy-policy' },
            { text: '寄付・開発支援', link: '/donate' }
          ]
        }
      ]
    },

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
