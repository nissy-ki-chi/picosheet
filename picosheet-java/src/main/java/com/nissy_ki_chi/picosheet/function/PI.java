package com.nissy_ki_chi.picosheet.function;

import java.math.BigDecimal;
import java.math.MathContext;

import com.nissy_ki_chi.picosheet.core.Element;
import com.nissy_ki_chi.picosheet.core.Function;
import com.nissy_ki_chi.picosheet.core.Resolver;
import com.nissy_ki_chi.picosheet.core.Element.ElementType;

public class PI extends Function {

	@Override
	public Element call(Element[] elements, MathContext mc, Resolver resolver) {
		return Element.newElement(ElementType.NUMBER, new BigDecimal(Math.PI));
	}

}
