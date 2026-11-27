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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

/**
 * 組み込みの関数です(DESIGN.md 2.6)。<br>
 * TODO: 今の function パッケージの関数は、ここに移してから消す。
 */
final class BuiltinFunctions {

	private BuiltinFunctions() {
	}

	static void registerTo(FunctionRegistry registry) {
		registry.register("SUM", FunctionSpec.of(ParameterKind.ANY).withRepeated(ParameterKind.ANY), BuiltinFunctions::sum);
		registry.registerLazy("IF", BuiltinFunctions::ifFunction);
	}

	/**
	 * SUM(値, ...)<br>
	 * 直接書いた値は数値に変換して足し(="1" や TRUE も数える)、変換できなければ #VALUE! にします。
	 * 範囲(セルの参照も含む)の中は数値だけを足し、文字列・論理値・空は無視します(DESIGN.md 1.2)。
	 */
	static Value sum(List<Argument> args, FunctionContext context) {
		MathContext mc = context.mathContext();
		BigDecimal total = BigDecimal.ZERO;
		for (Argument arg : args) {
			if (arg instanceof Argument.Single(Value value)) {
				Value number = Evaluator.toNumeric(value);
				if (!(number instanceof Value.Numeric(BigDecimal n))) {
					return number;
				}
				total = total.add(n, mc);
			} else {
				for (Value value : Evaluator.valuesOf(arg)) {
					if (value instanceof Value.Numeric(BigDecimal n)) {
						total = total.add(n, mc);
					}
				}
			}
		}
		return new Value.Numeric(total);
	}

	/**
	 * IF(条件, 真の場合, [偽の場合])<br>
	 * 使う側の引数だけを計算します。偽の場合を省略して条件が偽なら FALSE を返します。
	 */
	static Value ifFunction(LazyFunction.Arguments args) {
		if (args.size() < 2 || 3 < args.size()) {
			return Evaluator.WRONG_VALUE;
		}
		Value condition = Evaluator.toBool(args.evaluate(0));
		if (!(condition instanceof Value.Bool(Boolean b))) {
			return condition;
		}
		if (b) {
			return args.evaluate(1);
		}
		return args.size() == 3 ? args.evaluate(2) : new Value.Bool(false);
	}

}
