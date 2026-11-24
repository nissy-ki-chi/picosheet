/*
 * Picosheet: Spreadsheet engine for Java Applications
 * Copyright (C) 2011-2026 yusuke nishikawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nissy_ki_chi.picosheet.core;

/**
 * テストで、Expr の木の形だけを比べるための補助です。<br>
 * Expr の equals() は位置も比べます。木の形が正しいかを確かめるテストでは位置を気にしなくてよいように、
 * すべての位置を0にした木を作ります。位置そのものは FormulaParserPositionTest で確かめます。
 */
final class ExprShape {

	private ExprShape() {
	}

	/**
	 * すべての部分の木の位置を0にした、同じ形の木を返します。
	 * @param expr 元の木
	 * @return 位置を0にした木
	 */
	static Expr of(Expr expr) {
		return switch (expr) {
			case Expr.Unary unary -> new Expr.Unary(unary.operatorType(), of(unary.operand()), 0);
			case Expr.Binary binary -> new Expr.Binary(binary.operatorType(), of(binary.leftOperand()), of(binary.rightOperand()), 0);
			case Expr.Function function -> new Expr.Function(function.functionName(),
					function.arguments().stream().map(ExprShape::of).toList(), 0);
			case Expr.Reference reference -> new Expr.Reference(reference.reference(), 0);
			case Expr.GroupReference group -> new Expr.GroupReference(group.groupReference(), 0);
			case Expr.TableReference table -> new Expr.TableReference(table.tableReference(), 0);
			case Expr.Literal literal -> new Expr.Literal(literal.value(), 0);
		};
	}

}
