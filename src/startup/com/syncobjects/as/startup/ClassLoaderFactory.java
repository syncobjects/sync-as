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
package com.syncobjects.as.startup;

import java.io.File;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 
 * @author dfroz
 *
 */
public class ClassLoaderFactory {
	public static void scandir(Set<URL> urls, File dir) throws Exception {
		if(!dir.isDirectory())
			throw new IllegalArgumentException("dir");
		urls.add(dir.toURI().toURL());
		for(File file: dir.listFiles()) {
			if(!file.canRead())
				continue;
			if(file.isDirectory()) {
				scandir(urls, file);
			}
			urls.add(file.toURI().toURL());
		}
	}

	public static ClassLoader createClassLoader(File files[], final ClassLoader parent) throws Exception {
		Set<URL> set = new LinkedHashSet<URL>();
		for(int i=0; i < files.length; i++) {
			File file = files[i];
			if(!file.canRead())
				continue;
			if(file.isDirectory())
				scandir(set, file);
		}

		final URL[] urls = set.toArray(new URL[set.size()]);
		
		StandardClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<StandardClassLoader>() {
			public StandardClassLoader run() {
				return new StandardClassLoader(urls, parent);
			}
		});
		
		return loader;
	}
}
