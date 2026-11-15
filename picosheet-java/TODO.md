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
  - **設計**：作り直しが必要な、公開APIや内部の作りの問題
- 特に記載がないものは、このリポジトリのソース（0.2.0-SNAPSHOT。2026-11-15に全項目を再確認）で実際に動かして確認しています。

## 未対応

### 例外が発生するもの（呼び出し側の処理まで止まる）
| # | 種類 | 内容 | 再現例 | 関係する箇所 |
|---|---|---|---|---|
| 1 | 不具合 | テーブル参照で行番号・列番号に0を使うと、エラー値ではなく例外になる（範囲外の `R4C1` は `INVALID_REFERENCES` のエラー値になるので、扱いがそろっていない） | `t#R0C1+1`、`SUM(t#RxC0)` → `IllegalArgumentException` | `Table`、`Range`、`Resolver.getCellsFromTable` |
| 2 | 不具合 | `AVERAGE` に数値が1つもないと0で割ってしまう | `AVERAGE(str)`（文字列のセルのみ）→ `ArithmeticException` | `function/AVERAGE` |
| 3 | 不具合 | 別シートのグループを参照すると例外になる | `SUM(other!g@)` → `IllegalArgumentException: Invalid Sheetname other` | `Resolver.getCellsFromGroup` |
| 4 | 不具合 | 真偽値に単項の `-` を付けると例外になる | `-true` → `IllegalStateException` | 原因は未調査（`FormulaParser.checkInfixNotation` の単項演算子のチェック、または `Calculator.operateArithmetic` の `UNARY_MINUS` の分岐が怪しい） |
| 5 | 不具合 | `TextLoader` の `begincell`～`endcell` の中に空行やコメント行があると例外になる（0.1.3で確認。0.1.4でもこの処理は変わっていない） | `begincell` の直後に空行を入れる → `IllegalStateException: No match found` | `util/TextLoader.CellCreator.process` |
| 27 | 不具合 | シートに `INVALID_FORMULA` のセルが1つでもあると、`setMathContext` で例外になる（式を読み取れなかったセルは RPN が `null` のまま、再計算の対象になる） | `sum(1)` のセルを作ってから `sheet.setMathContext(new MathContext(34))` → `NullPointerException` | `Sheet.setMathContext`、`Calculator.recalcEnable`、`Calculator.doCalc` |

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
| 29 | 不具合 | `Group.addCell(String)` が、グループのあるシートではなくデフォルトシート（最初のシート）からセルを探す | 2つ目のシート `other` で `other.addGroup("g@").addCell("y")` → `ReferenceNotFoundException: cellname y is not found` | `Group.addCell(String)`（`Resolver.getCell` にシート名を渡していない） |

### 作りの問題・足りない機能
| # | 種類 | 内容 | 関係する箇所 |
|---|---|---|---|
| 20 | 機能 | `ROUND`、`MAX`、`MIN`、`COUNT`、`VLOOKUP` などの関数がない | `function/`、`BuiltinFunctionsConstants` |
| 21 | 機能 | 自分で作った関数を、実行時に登録する公開APIがない（今は `BuiltinFunctionsConstants` を書き換えるしかない） | `Book`、`FunctionFactory` |
| 22 | 機能 | `ElasticBookWrapper` などのラッパーが作りかけ（`deleteSheet` が何もしない、など） | `ElasticBookWrapper`、`ElasticSheetWrapper`、`ElasticCellWrapper` |
| 23 | 仕様 | 桁の大きい数を `getValueString()` で取り出すと、指数表記になることがある（`1234567890123456789` → `1.234567890123457E+18`） | `Cell.getValueString` |
| 28 | 仕様 | `Sheet.getCells()` と `getCellNames()` にテーブルの中のセルが含まれない。シートの全セルを調べるには、テーブルを別に回す必要がある | `Sheet`、`Table` |

### 設計の見直し（0.2.0で作り直す）
0.2.0 はまだ公開していないので、公開APIの互換は保たずに作り直します（2026-11-15に決定）。式の解析（`FormulaParser`）と、依存関係の追跡（`Resolver` の参照の表と、「計算必要」の印を付けてから必要なセルを計算する仕組み）は残します。
決めた方針の詳細と理由は [DESIGN.md](DESIGN.md) に書きます。
ここの項目は、ソースを読んで確認したものです。#31 のメモリの件と #32 は、実際に動かして確認しています。上の不具合のうち、作り直しで解消するか形が変わるものは「関係する項目」に書いています。不具合を直すのは、関係する設計を決めてからにします。

| # | 種類 | 内容 | 方針の案 | 関係する箇所 | 関係する項目 |
|---|---|---|---|---|---|
| 30 | 設計 | 公開APIに内部の部品が出ている。`Element` が「セルの値」と「式の部品（演算子・関数・参照）」を1つの型で表しているので、値を取り出す `switch` に値でない種類が混ざる。`Resolver`（`setCurrentSheet` など）が public で、関数を作る人も直接使う。すべてが `core` パッケージにあり、`module-info` もないので内部を隠せない | **方針を決めた（2026-11-15、詳細は [DESIGN.md](DESIGN.md) の2章と3章）**：`Element` を「値（`Value`、公開）」「式（`Token` と `Expr`、内部）」「関数の引数（解決済みの値か値の並び）」に分ける。値は不変の sealed interface と record で表し、全体共通のキャッシュはなくす（空・`TRUE`・`FALSE` は定数で共有）。式の内部表現は RPN をやめて構文木（AST）にする。比較演算子は Excel と同じく二項演算子として左から順に計算する（連続比較はやめる）。計算のルールは Excel に準ずる（1章）。公開するパッケージと内部のパッケージを分け、`module-info` で内部を隠す。日付の型、関数の API、エラー値の情報、Java のバージョンは未決定 | `Element`、`Resolver`、`Function`、パッケージ構成 | #13、#17 |
| 31 | 設計 | スレッド安全でない。`Element.newElement` が static で `synchronized` のキャッシュ（`WeakHashMap`）を通るので、ブックが別でも全体で1つのロックを取り合う。このキャッシュは値にキーと同じオブジェクトを入れているので、エントリーが回収されず、メモリが解放されない（値を100万回入れ替えると約80MB増え、ブックを捨てても減らない。詳細は DESIGN.md の 2.4）。日付の書式（`SimpleDateFormat`）を static で共有しているので、別々のスレッドで別々のブックを使っても安全とは言えない。`ConcurrentBookWrapper` は API を手で包み直したもの（約500行）で、保守が大変 | static な可変の状態をなくし、「1つのブックは1つのスレッドで使う」ことを基本にする。値は不変にする。並行して使うための仕組みは、そのあとで決める | `Element`、`ConcurrentBookWrapper` | |
| 32 | 設計 | 値を1つ入れるたびにすぐ再計算し、計算は再帰で行う。長くつながった式で `StackOverflowError` になる（`c1`=`c0+1`、`c2`=`c1+1`、… と5000個つなぐと発生。1000個では起きない）。計算を止める手段は、ブック全体のフラグ（`recalcDisable`）しかない | まとめて値を入れてから1回だけ計算するAPI（バッチ）を用意する。計算の順番を再帰を使わずに決める（トポロジカルソートなど） | `Calculator.doCalc`、`Calculator.recalcIfRequired`、`Book.recalcEnable`/`recalcDisable` | #27 |
| 33 | 設計 | 日付を `java.util.Date` で扱い、タイムゾーンのずれを `getRawOffset()` で手で足し引きしている（夏時間を考慮していない） | `java.time`（`LocalDate`、`LocalDateTime`）に置き換える | `Cell.setDateValue`、`Cell.getValueString`、`Element` | #16 |
| 34 | 設計 | 関数を、クラス名の文字列とリフレクション（非推奨の `Class.newInstance`）で登録している。`Book` と `Calculator` がそれぞれ別の関数の一覧（`FunctionFactory`）を持ち、`Book.getFunctionNames` と計算で別の一覧を使っている | ブックごとに1つの関数の一覧（`FunctionRegistry` など）を持ち、ラムダやクラスで登録できる公開APIにする。関数名の大文字・小文字の扱いもここで決める | `FunctionFactory`、`BuiltinFunctionsConstants`、`Book`、`Calculator`、`Function` | #6、#20、#21 |
| 35 | 設計 | エラーの伝え方が、例外・エラー値・セルの状態 `ERROR` の3通りあり、使い分けが決まっていない。エラー値には、どのセルで、なぜ起きたかの情報がない | 式の中で起きたことは、すべてエラー値で返す（例外を呼び出し側に漏らさない）。エラー値に、元になったセルと理由を持たせる。ブック全体のエラーを一覧で取り出せるようにする | `Element.ErrorType`、`Calculator`、`Sheet.getErrorCells` | #1、#3、#4、#11、#12、#13 |
| 36 | 設計 | `Cell.equals` は中身で比べるのに、`hashCode` は名前から作っている（Java の約束に反する）。`Sheet.equals` は、`Sheet` 以外のオブジェクトと比べても `true` を返す | セルやシートは「同じオブジェクトかどうか」で比べる（`equals` を上書きしない）。中身を比べるメソッドが必要なら、別の名前で用意する | `Cell.equals`/`hashCode`、`Sheet.equals`/`hashCode` | |
| 37 | 設計 | 数値を `String` で渡す必要がある（`setNumberValue("1200")`）。`setValue(String)` は文字列から型を推測する（日付の書式をいくつも試す） | `BigDecimal`、`long`、`LocalDate` などを受け取るメソッドにする。文字列からの推測は、読み込み用の機能として分ける | `Cell.setValue`、`Cell.setNumberValue` | #23 |
| 38 | 設計 | 本体の jar に、試作のクラス（`HogeShell`、`HogeCacheShell`）と、作りかけのラッパー（`Elastic*`）が入っている | 試作のクラスは削除する。ラッパーは、必要かどうかを決めてから作り直すか削除する | `util/HogeShell`、`util/HogeCacheShell`、`Elastic*Wrapper` | #22 |
| 39 | 機能 | AIエージェントが計算の道具として使うための入出力がない。今あるのは独自のテキスト形式（`TextLoader`）と XML だけ | モデル全体（セル・式・グループ・テーブル）を JSON で渡し、すべての値とエラーをまとめて JSON で返す | `util/` | #5、#8 |
| 40 | 機能 | AIや人が計算を確かめるための機能がない。値がどのセルから計算されたかをたどれない。セルの数や依存の深さに上限を付けられない | 値の由来（依存しているセル）をたどれるAPIを用意する。セルの数や依存の深さに上限を設定できるようにする | `Resolver`、`Calculator` | #32 |

## 対応済み
- テストが `AllTests` のスイート経由でしか実行されず、登録されていないテストクラスが実行されていなかった問題（旧 #24。名前が `Test` で終わるクラスをすべて実行するように変更し、`AllTests` を削除）
- `SimpleWriterTest.testRead` が `testWrite` の出力したファイルに依存していた問題（旧 #25。それぞれのテストが一時ファイルに書き出してから読むように変更）
- ソースのヘッダーが「JPicosheet」の名前と LGPL 2.1 のままだった問題（旧 #26。全ソースを Apache License 2.0 のヘッダーに変更し、`pom.xml` にライセンスを記載）
- ライブラリ本体が JUnit を `import` していて、JUnit がないとコンパイルできない不具合（旧 #19、Maven への移行時に修正）
- XML保存で、テーブルの最後の行と列が欠ける不具合（dfa5a47）
- `AVERAGE` にグループやテーブルを渡すと例外になる不具合（0.1.4）
- 真偽値のセルで `getValueString()` が `""` を返す不具合（0.1.4）
- グループやテーブルの中のエラーが、集計の結果に反映されない不具合（0.1.4）
- エラーの種類の名前の綴り間違い（`CIRCULER_REFERENCE` を `CIRCULAR_REFERENCE` に修正、0.1.4）
