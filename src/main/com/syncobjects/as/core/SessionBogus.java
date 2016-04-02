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
package com.syncobjects.as.core;

import com.syncobjects.as.api.SessionContext;

/**
 * @author dfroz
 */
public class SessionBogus extends Session {
	private static final long serialVersionUID = 4501346595414405798L;
	private static final SessionContext context = new SessionContextBogus();
	
	@Override
	public SessionContext getSessionContext() {
		return context;
	}
}

class SessionContextBogus extends SessionContext {
	private static final long serialVersionUID = -432846513263222879L;
	
	@Override
	public Object put(String key, Object value) {
		return null;
	}
}