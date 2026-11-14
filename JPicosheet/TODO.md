# Picosheet TODO

まだ直していない不具合と、検討中の仕様をまとめた一覧です。人もAIも参照します。

## 使い方
- 新しく問題を見つけたら、表に1行追加してください。「再現例」には、式やコードを動かせる形で書きます。
- 直したら、その行を表から消し、「対応済み」に1行で残します（コミットのハッシュがあれば一緒に書きます）。
- 番号は付け直しません。新しい項目には、使われていない次の番号を付けます。
- 「種類」の意味は次のとおりです。
  - **不具合**：例外が出る、結果が明らかにおかしい
  - **仕様**：意図した動作か要検討、またはExcelと違う
  - **機能**：まだない機能
- 特に記載がないものは、0.1.4（このリポジトリ、2026-11-04時点）で実際に動かして確認しています。

## 未対応

### 例外が発生するもの（呼び出し側の処理まで止まる）
| # | 種類 | 内容 | 再現例 | 関係する箇所 |
|---|---|---|---|---|
| 1 | 不具合 | テーブル参照で行番号・列番号に0を使うと、エラー値ではなく例外になる（範囲外の `R4C1` は `INVALID_REFERENCES` のエラー値になるので、扱いがそろっていない） | `t#R0C1+1`、`SUM(t#RxC0)` → `IllegalArgumentException` | `Table`、`Range`、`Resolver.getCellsFromTable` |
| 2 | 不具合 | `AVERAGE` に数値が1つもないと0で割ってしまう | `AVERAGE(str)`（文字列のセルのみ）→ `ArithmeticException` | `function/AVERAGE` |
| 3 | 不具合 | 別シートのグループを参照すると例外になる | `SUM(other!g@)` → `IllegalArgumentException: Invalid Sheetname other` | `Resolver.getCellsFromGroup` |
| 4 | 不具合 | 真偽値に単項の `-` を付けると例外になる | `-true` → `IllegalStateException` | 原因は未調査（`FormulaParser.checkInfixNotation` の単項演算子のチェック、または `Calculator.operateArithmetic` の `UNARY_MINUS` の分岐が怪しい） |
| 5 | 不具合 | `TextLoader` の `begincell`～`endcell` の中に空行やコメント行があると例外になる（0.1.3で確認。0.1.4でもこの処理は変わっていない） | `begincell` の直後に空行を入れる → `IllegalStateException: No match found` | `util/TextLoader.CellCreator.process` |

### 結果が間違っている、または書いた式が通らないもの
| # | 種類 | 内容 | 再現例 | 関係する箇所 |
|---|---|---|---|---|
| 6 | 不具合 | 関数名を大文字で書かないと認識されない（0.1.3では小文字でも使えた） | `sum(1,2)`、`Sum(1,2)` → `INVALID_FORMULA` | `FunctionFactory`（`hasFunction` などで大文字に変換する処理が外された） |
| 7 | 不具合 | `addRow()` を呼んでも `getRowSize()` が増えない（追加した行のセルが使えるかどうかは未確認） | 3行のテーブルで `addRow()` → `getRowSize()` が3のまま | `Table.addRow`、`RowsChunk` |
| 8 | 不具合 | `TextLoader` でシートを2つ以上書いても、セルがすべて最初のシートに入る | `beginsheet name=s2` の中に書いたセルが `s1` に入る | `util/TextLoader.loadSheet`（作業中のシートを切り替えていない） |
| 9 | 不具合 | 別シートのテーブルを参照できない | `other!t#R1C1`、`SUM(other!t#)` → `INVALID_FORMULA` | `FormulaParser`（`シート名!セル名` のパターンが先に一致してしまう） |
| 10 | 不具合 | `true` や `false` で始まる名前のセルを参照できない | `trueValue+1` → `INVALID_FORMULA` | `FormulaParser`（真偽値のパターンがセル名より先に一致する） |
| 11 | 不具合 | エラー値が表に出ないまま計算が進む | `SUM(1/0, 2)` → 2、`IF(1/0>1, 1, 2)` → 2 | `function/SUM`、`Calculator.operateComparison` |
| 12 | 不具合 | 計算を重ねると、最初に起きたエラーの種類が分からなくなる | `1/0+1` → `WRONG_VALUE`（`DIVIDE_BY_ZERO` にならない） | `Calculator.operateArithmetic` |
| 13 | 仕様 | `getErrorCells()` や `containsErrorCell()` で見つかるのは、式を読み取れなかったエラーだけ。0除算・参照切れ・循環参照は含まれない | `1/0` のセルが `getErrorCells()` に含まれない | `Sheet`、`Cell.setFormula`、`Calculator.doCalc` |
| 14 | 仕様 | `*` `/` `^` がすべて同じ優先順位で、左から順に計算される（Excelでは `^` のほうが先） | `2*3^2` → 36（Excelでは18） | `Element.Operator` の優先順位 |
| 15 | 仕様 | 文字列リテラルを比較に使えない。文字列のセルどうしなら比較できる | `"a"="a"`、`x="5"` → `INVALID_FORMULA` | `FormulaParser.isSuitableElementsOfComparison` |
| 16 | 仕様 | 文字列どうしの大小比較と、日付どうしの比較が、常に false になる | `str1<str2` → false | `Calculator.operateComparison`（コード中にTODOあり） |
| 17 | 仕様 | 空のセルは計算では0として扱われるのに、0と比較すると等しくならない | `empty+1` → 1、`empty=0` → false | `Calculator.operateComparison`（型が違うと false にしている） |
| 18 | 仕様 | `^` は `double` に変換して計算するので、`BigDecimal` の精度が失われる | `2^0.5` → `1.41421356237309514547...` | `Calculator.operateArithmetic`（`POWER` の分岐） |

### 作りの問題・足りない機能
| # | 種類 | 内容 | 関係する箇所 |
|---|---|---|---|
| 19 | 不具合 | ライブラリ本体が JUnit を `import` しているので、JUnit がないとコンパイルできない（使っていない `import static org.junit.Assert.assertArrayEquals`） | `Resolver` |
| 20 | 機能 | `ROUND`、`MAX`、`MIN`、`COUNT`、`VLOOKUP` などの関数がない | `function/`、`BuiltinFunctionsConstants` |
| 21 | 機能 | 自分で作った関数を、実行時に登録する公開APIがない（今は `BuiltinFunctionsConstants` を書き換えるしかない） | `Book`、`FunctionFactory` |
| 22 | 機能 | `ElasticBookWrapper` などのラッパーが作りかけ（`deleteSheet` が何もしない、など） | `ElasticBookWrapper`、`ElasticSheetWrapper`、`ElasticCellWrapper` |
| 23 | 仕様 | 桁の大きい数を `getValueString()` で取り出すと、指数表記になることがある | `1234567890123456789` → `1.234567890123457E+18` | `Cell.getValueString` |

## 対応済み
- XML保存で、テーブルの最後の行と列が欠ける不具合（dfa5a47）
- `AVERAGE` にグループやテーブルを渡すと例外になる不具合（0.1.4）
- 真偽値のセルで `getValueString()` が `""` を返す不具合（0.1.4）
- グループやテーブルの中のエラーが、集計の結果に反映されない不具合（0.1.4）
- エラーの種類の名前の綴り間違い（`CIRCULER_REFERENCE` を `CIRCULAR_REFERENCE` に修正、0.1.4）
