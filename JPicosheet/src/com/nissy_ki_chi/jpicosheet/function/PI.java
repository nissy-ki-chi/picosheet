package com.nissy_ki_chi.jpicosheet.function;

import java.math.BigDecimal;
import java.math.MathContext;

import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.Function;
import com.nissy_ki_chi.jpicosheet.core.Resolver;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;

public class PI extends Function {

	@Override
	public Element call(Element[] elements, MathContext mc, Resolver resolver) {
		return Element.newElement(ElementType.NUMBER, new BigDecimal(Math.PI));
	}

}
