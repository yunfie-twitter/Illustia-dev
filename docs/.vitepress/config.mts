import { defineConfig } from 'vitepress'

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
    }
  }
})
