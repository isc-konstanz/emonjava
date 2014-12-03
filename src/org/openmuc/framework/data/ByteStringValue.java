/*
 * Copyright 2011-13 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openmuc.framework.data;

/**
 * Represents an immutable array of bytes.
 * 
 */
public final class ByteStringValue implements Value {

	final byte[] value;

	public ByteStringValue(final byte[] value) {
		this.value = new byte[value.length];
		System.arraycopy(value, 0, this.value, 0, value.length);
	}

	@Override
	public double asDouble() {
		throw new TypeConversionException();
	}

	@Override
	public float asFloat() {
		throw new TypeConversionException();
	}

	@Override
	public long asLong() {
		throw new TypeConversionException();
	}

	@Override
	public int asInt() {
		throw new TypeConversionException();
	}

	@Override
	public short asShort() {
		throw new TypeConversionException();
	}

	@Override
	public byte asByte() {
		throw new TypeConversionException();
	}

	@Override
	public boolean asBoolean() {
		throw new TypeConversionException();
	}

	/**
	 * Returns a copy of the internal byte array.
	 * 
	 * @return A copy of the internal byte array.
	 */
	@Override
	public byte[] asByteArray() {
		byte[] byteArrayCopy = new byte[value.length];
		System.arraycopy(value, 0, byteArrayCopy, 0, byteArrayCopy.length);
		return byteArrayCopy;
	}

	@Override
	public String toString() {
		return new String(value);
	}
}
