# Picosheet リファレンス（AI向け）

JavaのコードからPicosheetを使う計算処理を書くときの資料です。
**Excelの知識をそのまま当てはめると間違える点**を中心にまとめています。
各項目は、このリポジトリのソース（0.2.0-SNAPSHOT、中身は0.1.4と同じ）を実際に動かして確かめています（2026-11-15、JDK 25）。

不具合の詳しい内容と再現例は [TODO.md](TODO.md) にあります。本文の「(#6)」などは TODO.md の番号です。不具合が直ったら、この資料も合わせて直してください。

---

## 1. 最小の例

```java
import com.nissy_ki_chi.picosheet.core.*;

Book book = new Book("quote");            // ブック名は英字か _ で始まる英数字と _
Sheet s = book.addSheet("main");          // 最初に追加したシートが「デフォルトシート」になる

s.addCell("price").setNumberValue("1200"); // 数値は文字列で渡す（中身は BigDecimal）
s.addCell("qty").setNumberValue("3");
Cell total = s.addCell("total").setFormula("price * qty");   // 先頭の "=" はあってもなくてもよい

total.getValue().getNumber();             // BigDecimal 3600
s.getCell("qty").setNumberValue("5");     // 参照しているセルは自動で再計算される
total.getValue().getNumber();             // BigDecimal 6000
```

- Mavenで使う場合の座標は `com.nissy-ki-chi:picosheet:0.2.0-SNAPSHOT` です（まだ公開していないので、`mvnw.cmd install` でローカルに入れて使います）。Java 17 以降で動きます。
- `addCell` は、同じ名前のセルがすでにあればそのセルを返します（新しく作り直しません）。
- セルを定義する順番は自由です。まだ存在しないセルを参照すると、その式はいったん `INVALID_REFERENCES` エラーになりますが、参照先のセルを作って値を入れた時点で再計算されます。

---

## 2. 名前の付け方

| 種類 | 書き方 | 例 | 備考 |
|---|---|---|---|
| セル | `[a-zA-Z_][a-zA-Z0-9_]*` | `unitPrice` | 大文字と小文字は区別される |
| グループ | 名前の最後に `@` | `east@` | 作るとき・参照するとき、どちらも `@` を付ける |
| テーブル | 名前の最後に `#` | `sales#` | 作るとき・参照するとき、どちらも `#` を付ける |
| テーブル内のセル | `テーブル名#R行C列` | `sales#R1C2` | **番号は1から始まる**（Excelと同じく `R1C1` が左上）。`R`/`C` は**大文字のみ** |
| 別シートのセル | `シート名!セル名` | `other!x` | 普通のセルだけ（下の注意を参照） |

### やってはいけないこと
- **テーブルの番号に `0` を使わない**。`t#R0C1` や `SUM(t#RxC0)` は、エラー値ではなく**例外**（`IllegalArgumentException`）になり、呼び出し側の処理まで止まります（#1）。範囲より大きい番号（3行のテーブルで `t#R4C1`）は `INVALID_REFERENCES` のエラー値になります。
- **`true` や `false` で始まるセル名を付けない**（`trueValue`、`falseCount` など）。式で参照すると `INVALID_FORMULA` になります（#10）。
- Excel風の `A1` や `B2:B10` は使えません。`A1` は「A1という名前のセル」として扱われます。
- 別シートのテーブル（`other!t#R1C1`、`SUM(other!t#)`）は `INVALID_FORMULA` になります（#9）。別シートのグループ（`SUM(other!g@)`）は例外が発生します（#3）。**シートをまたいで参照できるのは普通のセルだけ**です。

---

## 3. 式の書き方

### 値（リテラル）
| 種類 | 書き方 | 注意 |
|---|---|---|
| 数値 | `12`、`3.5`、`.5` | 指数表記（`1e3`）や桁区切り（`1,000`）は使えない。負の数は単項の `-` で書く |
| 文字列 | `"abc"` | 中に `"` を書くエスケープ方法はない |
| 真偽値 | `true`、`false` | **小文字のみ** |

### 演算子と優先順位（**Excelと違う点に注意**）

| 優先順位（高い順） | 演算子 |
|---|---|
| 4 | 単項の `+` `-` |
| 3 | `*` `/` `^` （**3つとも同じ順位で、左から順に計算**） |
| 2 | `+` `-` |
| 1 | `=` `<>` `<` `<=` `>` `>=` |
| 0 | `&`（文字列の連結。**一番低い**） |

実際の計算結果:
```
2*3^2   → 36   （Excelでは18）   ← 必ず 2*(3^2) と括弧を付ける
2^3^2   → 64   （左から順に計算）
-2^2    → 4
"a" & 1+2 → "a3"
```
**`^` を使うときは必ず括弧で囲んでください**（#14）。
また `^` は内部で `double` に変換して計算するので、`2^0.5` は `1.41421356237309514547...` のように誤差のある長い値になります（#18）。

### 比較を続けて書ける
`1<x<9` は `1<x AND x<9` と同じ意味になります（Excelとは違います）。

### 文字列リテラルは比較に使えない
- `"a"="a"`、`x="5"`、`str&"x"=str` → **`INVALID_FORMULA`**（#15）
- 文字列が入ったセルどうしの比較（`str1=str2`）はできます。
- 文字列リテラルは `&` の前後か、関数の引数（例: `IF(c, "big", "small")`）にしか書けません。

### 型ごとの扱い
- 空のセルは、足し算・掛け算などでは `0` として扱われます（`empty+1` → 1）。
- **ただし比較では `0` と等しくなりません**（`empty=0` → false、#17）。比較では型が違うと常に false になります。
- 真偽値は計算では `1` か `0` として扱われます（`true+1` → 2）。ただし `-true` と書くと例外が発生します（#4）。
- 文字列に計算を行うと `WRONG_VALUE` になります。
- 文字列どうしの大小比較（`<` など）と、日付どうしの比較は、**常に false** になります（まだ実装されていません、#16）。

### グループとテーブルの範囲は、関数の引数にしか書けない
`SUM(g@)+1` は書けますが、`g@+1` は `INVALID_FORMULA` になります。テーブルの1つのセル（`t#R2C1*2`）は普通のセルと同じように式に書けます。

### テーブルの範囲の書き方
`x` は「その行（列）の全部」という意味です。

| 書き方 | 意味 |
|---|---|
| `t#` | テーブル全体 |
| `t#RxC2` | 2列目の全行 |
| `t#R1Cx` | 1行目の全列 |
| `t#R1C1:R4C2` | 四角形の範囲（`t#R1C1:RxC2` のように終点に `x` を使ってもよい） |

---

## 4. 関数

**関数名は大文字で書いてください**。`sum(1,2)` や `Sum(1,2)` は `INVALID_FORMULA` になります（#6。0.1.3では小文字でも使えました）。

| 関数 | 動作と注意点 |
|---|---|
| `SUM(...)` | 数値と真偽値を合計する。グループ・テーブルを渡せる。グループ・テーブルの中にエラーのセルがあれば、結果もエラーになる。**ただし引数に直接書いた式のエラーと、文字列は何も言わずに無視する**（`SUM(1/0, 2)` → 2、#11） |
| `AVERAGE(...)` | グループ・テーブルを渡せる。**数値が1つもないと例外が発生する**（`AVERAGE(str)` → `ArithmeticException`、#2） |
| `IF(条件, 真のとき, 偽のとき)` | 条件は真偽値でなければならない（`IF(1,...)` は `INVALID_PARAMETER`）。両方の値が先に計算される |
| `AND(...)`, `OR(...)`, `NOT(x)` | 真偽値を受け取る |
| `ABS(x)`, `SQRT(x)`, `MOD(a,b)` | `MOD(-7,3)` → 2（Excelと同じく、結果の符号は割る数に合わせる） |
| `INT(x)` | 小数点以下を切り捨てて小さいほうの整数にする（`INT(-1.5)` → -2） |
| `SIN`, `COS`, `TAN`, `RADIANS`, `DEGREES`, `PI()` | `double` で計算するので、結果は誤差を含む長い桁になる（`PI()` → `3.14159265358979311599...`） |

**上の表にない関数（`ROUND`、`MAX`、`MIN`、`COUNT`、`VLOOKUP` など）は存在しません**（#20）。書くと式全体が `INVALID_FORMULA` になります。

### 関数を追加する方法
`Function` を継承したクラスを作り、`BuiltinFunctionsConstants.BUILTIN_FUNCTIONS` に `{"関数名(大文字)", "クラスの完全修飾名"}` を追加します。公開APIを使って実行時に登録する方法は、まだありません（#21）。

```java
package com.nissy_ki_chi.picosheet.function;

public class MAX extends Function {
    @Override
    public Element call(Element[] args, MathContext mc, Resolver resolver) {
        // args には評価済みの値（NUMBER, STRING, BOOLEAN, EMPTY, ERROR）か、
        // GROUP_REFERENCE / TABLE_REFERENCE が入る。後の2つは
        // resolver.getCellsFromGroup(getGroupReference(e)) や
        // resolver.getCellsFromTable(getTableReference(e)) でセルの集まりを取り出す。
        // 失敗したときは例外を投げず、Element.newElement(ElementType.ERROR, ErrorType.XXX) を返す。
    }
}
```
既存の `function/SUM.java` や `function/AVERAGE.java` が参考になります。

---

## 5. 値を取り出す

```java
Element v = cell.getValue();
switch (v.getType()) {                    // Element.ElementType
    case NUMBER:  v.getNumber();  break;  // BigDecimal
    case STRING:  v.getString();  break;
    case BOOLEAN: v.getBoolean(); break;
    case ERROR:   v.getErrorType(); break;// Element.ErrorType
    case EMPTY:   break;
}
```

- `getValueString()` は、真偽値なら `"true"` / `"false"` を返します。
- `getValueString()` は、桁が大きい数を `1.234567890123457E+18` のような指数表記で返すことがあります（#23）。表示するときは `getValue().getNumber().toPlainString()` を使ってください。

### 計算の精度
- シートごとの精度は初期値が `MathContext.DECIMAL64`（有効数字16桁）です。`setNumberValue` で入れた値も、この桁数に丸められます（`1234567890123456789` → `1234567890123457000`）。
- `sheet.setMathContext(new MathContext(34, RoundingMode.HALF_UP))` のように変更できます。変更するとシート全体が再計算されます。**ただし、シートに `INVALID_FORMULA` のセルが1つでもあると `NullPointerException` が発生します**（#27）。精度は、セルを作る前に設定してください。
- `0.1+0.2` → `0.3`（誤差は出ません）。`1/3` → `0.3333333333333333`（16桁）。

---

## 6. エラーの見つけ方（**重要**）

`sheet.containsErrorCell()` や `getErrorCells()` で見つかるのは、**式を読み取れなかったもの（`INVALID_FORMULA`）だけ**です（#13）。
計算中に起きたエラー（0除算、存在しないセルの参照、循環参照、引数の誤りなど）は見つかりません。

| 例 | 値 | `getStatus()` | `getErrorCells()` に含まれるか |
|---|---|---|---|
| `ROUND(1.5)`, `1e3`, `sum(1,2)` | `INVALID_FORMULA` | ERROR | 含まれる |
| `noSuchCell+1`, `t#R4C1`（範囲外） | `INVALID_REFERENCES` | ERROR | **含まれない** |
| 循環参照 | `CIRCULAR_REFERENCE` | ERROR | **含まれない** |
| `SUM(g@)`（`g@` の中に `1/0` のセル） | `DIVIDE_BY_ZERO` | ERROR | **含まれない** |
| `1/0` | `DIVIDE_BY_ZERO` | CALCULATED | **含まれない** |
| `1/0+1` | `WRONG_VALUE`（元のエラーの種類は失われる、#12） | CALCULATED | **含まれない** |
| `IF(1, 2, 3)` | `INVALID_PARAMETER` | CALCULATED | **含まれない** |

さらに、**エラーが表に出てこないまま計算が進むことがあります**（#11）。
- `SUM` は、引数に直接書いた式のエラーを無視します。
- 比較では、エラーと数値を比べると false になります（`IF(1/0>1, 1, 2)` → 2）。

そのため、確かめるときは**必ずすべてのセルの値の型を見てください**。
**`sheet.getCells()` にはテーブルの中のセルが含まれない**ので、テーブルは別に調べます（#28）。

```java
for (Sheet sh : book.getSheets()) {
    List<Cell> cells = new ArrayList<>(sh.getCells());
    for (Table t : sh.getTables()) {
        for (int r = 1; r <= t.getRowSize(); r++) {       // 番号は1から
            for (int c = 1; c <= t.getColSize(); c++) {
                cells.add(t.getCell(r, c));
            }
        }
    }
    for (Cell c : cells) {
        if (c.getValueType() == Element.ElementType.ERROR) {
            System.err.println(c.getFullyQualifiedName() + " " + c.getFormula()
                    + " -> " + c.getValue().getErrorType());
        }
    }
}
```

---

## 7. まとめて値を入れる（テーブル・グループ・再計算の停止）

```java
Table t = s.addTable("sales#", rowCount, 3);    // 作った時点で sales#R1C1 などのセルができている
book.recalcDisable();                           // 大量に入れる間は再計算を止める
for (int i = 1; i <= rowCount; i++) {           // getCell(行, 列) も1から数える
    t.getCell(i, 1).setNumberValue(qty[i - 1]);
    t.getCell(i, 2).setNumberValue(price[i - 1]);
    t.getCell(i, 3).setFormula("sales#R" + i + "C1 * sales#R" + i + "C2");
}
book.recalcEnable();                            // ここでまとめて再計算される
s.addCell("total").setFormula("SUM(sales#RxC3)");

Group g = s.addGroup("east@");                  // 並びが決まっていないセルの集まり
g.addCell("tokyo").addCell("chiba");            // 先にセルを作っておく必要がある
s.addCell("eastTotal").setFormula("SUM(east@)");
```

- `recalcDisable()` の間に式を入れたセルは、値が空のまま `getStatus()` が `REQUIRE_CALCULATION` になります。`recalcEnable()` を呼ぶまで値を読まないでください。
- `t.getCell(0, 0)` のように0を渡すと例外になります。
- **`Group.addCell(String)` は、セル名をデフォルトシート（最初に追加したシート）から探します**（#29）。2つ目以降のシートでグループを作るときは、`g.addCell(sheet.getCell("x"))` のように `Cell` を渡してください。
- テーブルの `addRow()` は、呼んでも `getRowSize()` が増えません（#7）。テーブルは最初に必要な大きさで作ってください。

---

## 8. テキストファイルから読み込む（`util.TextLoader`）

```
# 「#」で始まる行はコメント（ただし begincell～endcell の中には書けない）
beginbook name=quote
beginsheet name=main
begincell
price=1200
qty=3
total==price*qty
endcell
begintable name=sales# rowsize=2 colsize=2
R1C1=1
R2C1==sales#R1C1*2
endtable
endsheet
endbook
```

```java
Book book = new TextLoader().load(new BufferedReader(new FileReader(path)));
```

- セルは1行に1つ、`名前=値` の形で書きます。`値` の先頭が `=` なら式になるので、**式の行は `名前==式` になります**。値が数値に見えれば数値、日付に見えれば日付、それ以外は文字列として入ります。
- テーブルの中のセルは、テーブル名を付けずに `R1C1=...` と書きます（番号は1から）。
- **`begincell`～`endcell` と `begintable`～`endtable` の中には、空行もコメントも書けません**。書くと `IllegalStateException: No match found` になります（#5）。
- **シートを2つ以上書いても、セルはすべて最初のシートに入ります**（#8。シート自体は作られます）。複数のシートが必要な場合は、Javaのコードで組み立ててください。
- ほかに、XML形式で保存・読み込みする `util.XMLWriter` / `util.XMLReader` もあります。

---

## 9. 書く前に確認すること

1. `^` を使う部分は括弧で囲んだか
2. 関数名を大文字で書いたか
3. 表にない関数（ROUND / MAX / MIN など）を使っていないか
4. `true`/`false` で始まるセル名を付けていないか
5. 文字列リテラルを比較に使っていないか
6. テーブルの番号は1から数えたか（式でも `getCell(行, 列)` でも）、`R`/`C` は大文字か
7. グループ・テーブルを、関数の引数以外（演算子など）に書いていないか
8. 別シートのテーブル・グループを参照していないか。2つ目以降のシートのグループに、セル名で `addCell` していないか
9. 精度を変えるなら、セルを作る前に `setMathContext` したか
10. 計算が終わったら、**テーブルの中も含めたすべてのセル**の `getValueType() == ERROR` を確認したか（`getErrorCells()` だけでは足りない）
