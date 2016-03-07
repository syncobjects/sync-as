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
 * @author dfroz
 */
public class BooleanArrayConverter implements Converter<Boolean[]> {
	public Boolean[] convert(String values[]) {
		if(values == null || values.length == 0)
			throw new IllegalArgumentException("values");
		Boolean booleans[] = new Boolean[values.length];
		for(int i=0; i < values.length; i++)
			booleans[i] = Boolean.valueOf(values[i]);
		return booleans;
	}
	
	public String toString() {
		return "BooleanArrayConverter";
	}
}