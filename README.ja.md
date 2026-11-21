# Picosheet

[English](README.md) | 日本語

Picosheet は、Java のプログラムの中でスプレッドシートのような計算を行うためのライブラリです。
名前を付けたセルに値や式を入れておくと、参照しているセルが変わったときに自動で再計算されます。画面（UI）は持たず、計算の部分だけを提供します。

- セルに名前を付けて、式から名前で参照できる（`price * qty` など）
- 値を変えると、そのセルを参照している式が自動で再計算される
- 数値は `BigDecimal` で計算するので、`0.1+0.2` が `0.3` になる
- 複数のセルをまとめる「グループ」と、行と列で並べる「テーブル」を使って、`SUM` などで集計できる
- シートの内容をテキストファイルや XML から読み込める

## 使い方

```java
import com.nissy_ki_chi.picosheet.core.*;

Book book = new Book("quote");
Sheet s = book.addSheet("main");

s.addCell("price").setNumberValue("1200");
s.addCell("qty").setNumberValue("3");
Cell total = s.addCell("total").setFormula("price * qty");

total.getValue().getNumber();             // 3600
s.getCell("qty").setNumberValue("5");     // total は自動で再計算される
total.getValue().getNumber();             // 6000
```

式の書き方、使える関数、Excel と違う点、エラーの調べ方は [AI_REFERENCE.md](picosheet-java/AI_REFERENCE.md) にまとめています。AI に Picosheet を使うコードを書かせるときにも、この資料を渡してください。

## 現在の状況

開発を再開したばかりで、バージョンは `0.2.0-SNAPSHOT` です。Maven Central にはまだ公開していません。
関数はまだ少なく（`SUM`、`AVERAGE`、`IF` など16個）、分かっている不具合もあります。まだ直していない不具合と、検討中の仕様は [TODO.md](picosheet-java/TODO.md) にあります。

## 必要なもの

- Java 21 以降（ビルドには JDK 21 以降が必要です）
- Maven は不要です。リポジトリに含まれている Maven Wrapper（`mvnw` / `mvnw.cmd`）が自動でダウンロードします。

## ビルド

`picosheet-java` フォルダで実行します。

```bash
cd picosheet-java
./mvnw test        # テストを実行する（Windows では mvnw.cmd test）
./mvnw package     # target/picosheet-0.2.0-SNAPSHOT.jar を作る
./mvnw install     # ほかのプロジェクトから使えるように、ローカルの Maven リポジトリに入れる
```

`install` した後は、ほかのプロジェクトの `pom.xml` に次のように書くと使えます。

```xml
<dependency>
    <groupId>com.nissy-ki-chi</groupId>
    <artifactId>picosheet</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

## フォルダの構成

| パス | 内容 |
|---|---|
| `picosheet-java/` | Java 版の本体（Maven プロジェクト） |
| `picosheet-java/src/main/java/com/nissy_ki_chi/picosheet/core/` | ブック、シート、セル、式の解析と計算 |
| `picosheet-java/src/main/java/com/nissy_ki_chi/picosheet/function/` | 組み込み関数（1つの関数が1つのクラス） |
| `picosheet-java/src/main/java/com/nissy_ki_chi/picosheet/util/` | テキストファイルや XML との読み書き |

## ライセンス

[Apache License 2.0](LICENSE) です。

以前は「JPicosheet」という名前で、LGPL 2.1 で公開していました。バージョン 0.1.4（タグ `v0.1.4`）までのソースは LGPL 2.1 のままです。
