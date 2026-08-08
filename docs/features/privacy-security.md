---
title: プライバシー & セキュリティ保護仕様
description: アプリロック、生体認証、電卓偽装モード、タスク隠蔽、スクリーンショット保護
---

# プライバシー & セキュリティ保護仕様

個人データの保護、アプリの偽装表示、およびキャプション・スクリーンショット防止に関する詳細仕様です。

---

## アプリロック & 生体認証 (`AppLockScreen`)

- `appLockEnabled` (`Boolean`): 4〜8 桁の PIN 暗証番号による起動保護。
- `biometricEnabled` (`Boolean`): Android 生体認証 (`BiometricPrompt`) による指紋/顔解錠。
- `privacyModeAutoLockTiming`: アプリをバックグラウンドへ移行させてからロックがかかるまでの判定タイマー。
  - `immediate`: 即時ロック
  - `30s` / `1m` / `5m` / `10m`: 指定時間経過後にロック
  - `disabled`: 自動ロック無効

---

## 電卓偽装モード (`CalculatorScreen` / `CalculatorEngine`)

ロック画面を一般的な電卓 UI へ偽装するセキュリティ機能です。

### 1. 演算評価エンジン (`CalculatorEngine`)
実在する電卓として四則演算（加減乗除、小数点、単項マイナス）を正しく計算・評価します。

- **字句解析 (`tokenize`)**: 入力文字列から数値トークンおよび演算子 (`+`, `-`, `*`, `/`) を抽出。
- **再帰降下パーサー (Recursive Descent Parser)**:
  - `parseAddSub`: 加算・減算の評価
  - `parseMulDiv`: 乗算・除算の評価（ゼロ除算時は `NaN` 返却）
  - `parseUnary`: 単項符号の評価
  - `parsePrimary`: 数値リテラルの評価

### 2. ステルス解錠メカニズム
- **通常解錠**: 設定した PIN コードを数値として入力し、`=` ボタンを押すことでアンロック。
- **パターンC (右上隅タップ解錠)**:
  電卓画面の右上領域を指定時間内に連続タップすることで、非常用解錠コード入力ダイアログ (`OverlayDialog`) が起動。

### 3. 動的アイコン・アプリアンサンブル変更
- `dummyAppName`: ホーム画面上のアプリタイトルを「電卓」へ変更。
- `dummyIconVariant`: アプリアイコンを電卓風のアイコン (`ic_launcher_dummy`) へコンポーネント有効化 (`PackageManager.setComponentEnabledSetting`) により動的切り替え。

---

## 画面・画面キャプション保護 (`PrivacyModeSettingsScreen`)

- `secureWindow` (`FLAG_SECURE`):
  有効時、Android の Window フラグ `WindowManager.LayoutParams.FLAG_SECURE` をセットし、画面のスクリーンショット撮影、画面録画、および外部ディスプレイへのキャストを遮断。
- `hideRecents`:
  有効時、Android のタスクスイッチャー（最近使ったアプリ）画面でのプレビュー表示をぼかし・マスキング保護。
