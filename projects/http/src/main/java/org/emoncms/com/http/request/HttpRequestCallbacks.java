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
package org.emoncms.com.http.request;

import org.emoncms.com.EmoncmsException;
import org.emoncms.com.http.HttpEmoncms;


/**
 * Interface used to notify the {@link HttpEmoncms} 
 * implementation about request events
 */
public interface HttpRequestCallbacks {

	HttpEmoncmsResponse onRequest(String parent, HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method)
		throws EmoncmsException;
	
	HttpEmoncmsResponse onRequest(String parent, HttpRequestAuthentication authentication, HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method) 
			throws EmoncmsException;

}
