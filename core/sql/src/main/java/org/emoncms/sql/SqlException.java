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
package org.emoncms.sql;

import org.emoncms.EmoncmsException;

public class SqlException extends EmoncmsException {
	private static final long serialVersionUID = -2086956087655922865L;

	public SqlException() {
		super();
	}

	public SqlException(String s) {
		super(s);
	}

	public SqlException(Throwable cause) {
		super(cause);
	}

	public SqlException(String s, Throwable cause) {
		super(s, cause);
	}

}
