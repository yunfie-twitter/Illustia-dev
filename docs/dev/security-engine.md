---
title: セキュリティ & 電卓パーサー仕様
description: CalculatorEngine の構文解析パーサー、パターンC解錠、FLAG_SECURE
---

# セキュリティ & 電卓パーサー仕様

---

## 演算評価エンジン (`CalculatorEngine`)

- **字句解析 (`tokenize`)**: 入力文字列から数値および演算子 (`+`, `-`, `*`, `/`) を抽出。
- **再帰降下パーサー (Recursive Descent Parser)**:
  - `parseAddSub`: 加算・減算の評価
  - `parseMulDiv`: 乗算・除算の評価（ゼロ除算時は `NaN` 返却）
  - `parseUnary`: 単項符号の評価
  - `parsePrimary`: 数値リテラルの評価

---

## ステルス解錠メカニズム

- **通常解錠**: 設定 PIN コードを入力し、`=` ボタンタップで解除。
- **パターンC (右上隅タップ解錠)**: 右上領域の連続タップで緊急解錠ダイアログ (`OverlayDialog`) 起動。
- **動的コンポーネント切り替え**: `PackageManager.setComponentEnabledSetting` により、アプリ表示名 (`dummyAppName`) およびアイコン (`dummyIconVariant`) を動的変更。
