/* 
 * Copyright 2016-20 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
 * 
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.emoncms.hibernate;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public class ScaleIntegerDescriptor extends AbstractTypeDescriptor<Long> {
	private static final long serialVersionUID = -4711264359790671417L;

	// Factor should be greater or equal 1000 and of type Integer. Factor 1000 converts
	// milliseconds in seconds. 
	private static final String FACTOR_DEFAULT = "1000";
	private static final String FACTOR = "Factor";

	public static final JavaTypeDescriptor<Long> INSTANCE = 
    	      new ScaleIntegerDescriptor();
	
	private double factor;
    	 
	@SuppressWarnings("unchecked")
	protected ScaleIntegerDescriptor() {
		super(Long.class, ImmutableMutabilityPlan.INSTANCE);
		int intFactor = Integer.valueOf(System.getProperty(FACTOR, FACTOR_DEFAULT));
		if (intFactor < 1000) intFactor = 1000;
		factor = Double.valueOf(intFactor);
	}

	@Override
	public String toString(Long value) {
		return value.toString();
	}

	@Override
	public Long fromString(String string) {
		return Long.valueOf(string);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> X unwrap(Long value, Class<X> type, WrapperOptions options) {
		if (value == null) return null;
		Integer intValue = (int) Math.round(((long)value)/factor);
		value.intValue();
		return (X) intValue;
	}

	@Override
	public <X> Long wrap(X value, WrapperOptions options) {
		if (value == null) return null;
		if (Integer.class.isInstance(value)) {
			return (long) Math.round((Long) value * factor);
		}
		throw unknownWrap(value.getClass());
	}

}
