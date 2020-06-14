package com.nissy_ki_chi.jpicosheet.function;

import java.math.BigDecimal;
import java.math.MathContext;

import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.Function;
import com.nissy_ki_chi.jpicosheet.core.Resolver;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;

public class SQRT extends Function {

	@Override
	public Element call(Element[] elements, MathContext mc, Resolver resolver) {
		// elementのルートを返す
		return Element.newElement(ElementType.NUMBER,
				new BigDecimal(Double.toString(Math.sqrt(elements[0].getNumber().doubleValue()))));
	}

}
