/*
 * Copyright 2016 ISC Konstanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
