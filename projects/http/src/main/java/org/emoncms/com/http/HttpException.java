/* 
 * Copyright 2016-17 ISC Konstanz
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
package org.emoncms.com.http;

import java.io.IOException;


public class HttpException extends IOException {
	private static final long serialVersionUID = -2024942377965269078L;

	private String message = "Unknown HTTP error";

	public HttpException() {
	}

	public HttpException(String message) {
		this.message = message;
	}

	public HttpException(int httpCode) {
		this.message = "HTTP status code: " + httpCode;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
