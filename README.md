# Picosheet

English | [日本語](README.ja.md)

Picosheet is a Java library for doing spreadsheet-style calculations inside your own programs.
You put values and formulas into named cells, and when a cell changes, every formula that refers to it is recalculated automatically. Picosheet has no user interface; it provides only the calculation engine.

- Name your cells and refer to them by name in formulas (for example, `price * qty`)
- Change a value, and the formulas that depend on it are recalculated automatically
- Numbers are calculated with `BigDecimal`, so `0.1+0.2` gives `0.3`
- Collect cells into "groups", or lay them out in rows and columns as "tables", and aggregate them with functions such as `SUM`
- Load a sheet from a text file or from XML

## Usage

```java
import com.nissy_ki_chi.picosheet.core.*;

Book book = new Book("quote");
Sheet s = book.addSheet("main");

s.addCell("price").setNumberValue("1200");
s.addCell("qty").setNumberValue("3");
Cell total = s.addCell("total").setFormula("price * qty");

total.getValue().getNumber();             // 3600
s.getCell("qty").setNumberValue("5");     // total is recalculated automatically
total.getValue().getNumber();             // 6000
```

The formula syntax, the available functions, the differences from Excel, and how to check for errors are described in [AI_REFERENCE.md](picosheet-java/AI_REFERENCE.md) (in Japanese). Give this document to an AI assistant when you ask it to write code that uses Picosheet.

## Status

Development has just restarted, and the current version is `0.2.0-SNAPSHOT`. It is not yet published to Maven Central.
There are only a few functions so far (16, including `SUM`, `AVERAGE` and `IF`), and there are known bugs. Open bugs and specifications under discussion are listed in [TODO.md](picosheet-java/TODO.md) (in Japanese).

## Requirements

- Java 21 or later (building also requires JDK 21 or later)
- You don't need to install Maven. The Maven Wrapper in the repository (`mvnw` / `mvnw.cmd`) downloads it automatically.

## Building

Run these commands in the `picosheet-java` folder.

```bash
cd picosheet-java
./mvnw test        # run the tests (on Windows: mvnw.cmd test)
./mvnw package     # build target/picosheet-0.2.0-SNAPSHOT.jar
./mvnw install     # install into your local Maven repository so other projects can use it
```

After `install`, add the following to the `pom.xml` of another project to use Picosheet.

```xml
<dependency>
    <groupId>com.nissy-ki-chi</groupId>
    <artifactId>picosheet</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

## Project layout

| Path | Contents |
|---|---|
| `picosheet-java/` | The Java implementation (a Maven project) |
| `picosheet-java/src/main/java/com/nissy_ki_chi/picosheet/core/` | Books, sheets, cells, and formula parsing and evaluation |
| `picosheet-java/src/main/java/com/nissy_ki_chi/picosheet/function/` | Built-in functions (one class per function) |
| `picosheet-java/src/main/java/com/nissy_ki_chi/picosheet/util/` | Reading and writing text files and XML |

## License

Picosheet is licensed under the [Apache License 2.0](LICENSE).

It was previously published as "JPicosheet" under the LGPL 2.1. Source code up to version 0.1.4 (tag `v0.1.4`) remains under the LGPL 2.1.
