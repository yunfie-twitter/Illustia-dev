import { defineConfig } from 'vitepress'

<<<<<<< HEAD
const repository = 'https://github.com/yunfie-twitter/Palleria'
const icon = 'https://yunfi.f5.si/Palleria/repo/com.yunfie.illustia/en-US/icon.png'

export default defineConfig({
  lang: 'ja-JP',
  title: 'Palleria Docs',
  description: 'Palleriaのインストール、使い方、開発に関する公式ドキュメント',
  base: '/Palleria/',
  cleanUrls: true,
  lastUpdated: true,
  head: [
    ['link', { rel: 'icon', href: icon }],
    ['meta', { name: 'theme-color', content: '#090b0f' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:title', content: 'Palleria Docs' }],
    ['meta', { property: 'og:description', content: '創作を、もっと心地よく。Palleriaの公式ドキュメント。' }]
  ],
  themeConfig: {
    logo: icon,
    siteTitle: 'Palleria Docs',
    search: {
      provider: 'local',
      options: {
        translations: {
          button: {
            buttonText: 'ドキュメントを検索',
            buttonAriaLabel: 'ドキュメントを検索'
          },
          modal: {
            noResultsText: '該当するページが見つかりませんでした',
            resetButtonTitle: '検索をリセット',
            footer: {
              selectText: '選択',
              navigateText: '移動',
              closeText: '閉じる'
            }
          }
        }
      }
    },
    nav: [
      { text: 'ガイド', link: '/guide/' },
      { text: '開発', link: '/development/' },
      { text: 'GitHub', link: repository }
    ],
    sidebar: {
      '/guide/': [
        {
          text: 'はじめに',
          items: [
            { text: 'ガイド概要', link: '/guide/' },
            { text: 'インストール', link: '/guide/install' },
            { text: 'ログイン', link: '/guide/login' }
          ]
        },
        {
          text: '使い方',
          items: [
            { text: '主な機能', link: '/guide/features' },
            { text: '設定とプライバシー', link: '/guide/settings' },
            { text: 'よくある質問', link: '/guide/faq' }
          ]
        }
      ],
      '/development/': [
        {
          text: '開発者向け',
          items: [
            { text: '開発ガイド', link: '/development/' },
            { text: 'ビルド', link: '/development/build' },
            { text: 'アーキテクチャ', link: '/development/architecture' },
            { text: 'コントリビューション', link: '/development/contributing' }
          ]
        }
      ]
    },
    socialLinks: [{ icon: 'github', link: repository }],
    editLink: {
      pattern: `${repository}/edit/main/docs/:path`,
      text: 'GitHubでこのページを編集'
    },
    lastUpdated: {
      text: '最終更新'
    },
    outline: {
      level: [2, 3],
      label: 'このページの内容'
    },
    docFooter: {
      prev: '前のページ',
      next: '次のページ'
    },
    darkModeSwitchLabel: 'テーマ',
    lightModeSwitchTitle: 'ライトテーマに切り替え',
    darkModeSwitchTitle: 'ダークテーマに切り替え',
    sidebarMenuLabel: 'メニュー',
    returnToTopLabel: 'ページ上部へ戻る',
    langMenuLabel: '言語',
    externalLinkIcon: true,
    footer: {
      message: 'PalleriaはPixiv Inc.とは関係のない非公式クライアントです。',
      copyright: 'GNU General Public License v3.0'
=======
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
>>>>>>> b85653cb264de3137e8bad14d61ebfcb28489357
    }
  }
})
