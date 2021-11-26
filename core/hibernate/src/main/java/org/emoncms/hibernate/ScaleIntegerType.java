/* 
 * Copyright 2016-2021 ISC Konstanz
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

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.IntegerTypeDescriptor;

public class ScaleIntegerType extends AbstractSingleColumnStandardBasicType<Long> {
	private static final long serialVersionUID = -7171959951207336491L;
	
	public static final ScaleIntegerType INSTANCE = new ScaleIntegerType();
	
	public ScaleIntegerType() {
		super(IntegerTypeDescriptor.INSTANCE, ScaleIntegerDescriptor.INSTANCE);
	}

	@Override
	public String getName() {
		return "ScaleInteger";
	}

}
