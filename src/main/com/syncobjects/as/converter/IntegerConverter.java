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
package com.syncobjects.as.converter;

import com.syncobjects.as.api.Converter;

/**
 * 
 * @author dfroz
 *
 */
public class IntegerConverter implements Converter<Integer> {
	public Integer convert(String values[]) {
		if(values == null || values.length == 0 || values[0].equals(""))
			return null;
		if(values[0] == null || values[0].equals(""))
			return null;
		return Double.valueOf(values[0]).intValue();
	}
	public String toString() {
		return "IntegerConverter";
	}
}