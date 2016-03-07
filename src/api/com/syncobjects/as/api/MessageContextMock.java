/*
 * Copyright 2016 SyncObjects Ltda.
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
 */
package com.syncobjects.as.api;

import java.util.Locale;

public class MessageContextMock implements MessageContext {
	public String get(String key) {
		return get(key, new String[0]);
	}
	public String get(String key, String ... args) {
		return get(null, key, args);
	}
	public String get(Locale locale, String key) {
		return get(null, key, new String[0]);
	}
	public String get(Locale locale, String key, String ... args) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		for(int i=0; i < args.length; i++) {
			sb.append(", "+args[i]);
		}
		return sb.toString();
	}
}