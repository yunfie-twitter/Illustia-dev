---
title: プライバシー & セキュリティ保護
description: アプリロック、生体認証、電卓偽装モード、タスク隠蔽、スクリーンショット防止
---

# プライバシー & セキュリティ保護

アプリの起動保護、偽装表示、および画面情報の保護仕様です。

---

## アプリロック & 生体認証 (`AppLockScreen` / `AppLockSetupScreen`)

- **PIN ロック**: 4〜8 桁の暗証番号による保護 (`appLockEnabled`)。
- **生体認証**: Android 標準の BiometricPrompt を使用した指紋/顔認証 (`biometricEnabled`)。
- **自動ロックタイマー (`privacyModeAutoLockTiming`)**:
  - `immediate` (即時ロック)
  - `30s` / `1m` / `5m` / `10m`
  - `disabled` (自動ロックなし)

---

## 電卓偽装モード (`CalculatorScreen` / `CalculatorEngine`)

アプリの起動画面を電卓UIへ偽装し、特定操作でのみ本機能へアクセスできるようにするセキュリティ設定です。

- **四則演算パーサー (`CalculatorEngine`)**:
  - 再帰降下パーサーを実装し、数値、演算子 (`+`, `-`, `×`, `÷`)、小数点含む式を実際に評価。
- **解錠パターン**:
  - 設定した PIN コードを電卓に入力して `=` ボタンをタップすることでロック解除。
  - 右上隅の指定エリアを連続タップすることで緊急解錠ダイアログ（レスキュー入力）を呼び出し可能。
- **アプリアイコン・名称変更**:
  - `dummyAppName`: ホーム画面上の表示名を「電卓」に変更。
  - `dummyIconVariant`: アプリアイコンを電卓アイコン（`ic_launcher_dummy`）へ動的コンポーネント切り替え。

---

## 画面・キャプション保護 (`PrivacyModeSettingsScreen`)

- `secureWindow` (`FLAG_SECURE`):
  有効化時、Android システムのキャプション機能を遮断し、画面のスクリーンショット撮影および録画を禁止。
- `hideRecents`:
  Android の「最近使ったアプリ」（タスクスイッチャー）一覧で、アプリのプレビュー画面をマスク表示。
