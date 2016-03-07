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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.converter.ConverterFactory;
import com.syncobjects.as.i18n.MessageFactory;
import com.syncobjects.as.i18n.ResourceBundleMessageFactory;
import com.syncobjects.as.responder.ResponderFactory;
import com.syncobjects.as.util.FileUtils;
import com.syncobjects.as.util.StringUtils;

/**
 * 
 * @author dfroz
 *
 */
public class Application {
	private static final Logger log = LoggerFactory.getLogger(Application.class);
	private File base;
	private final ApplicationConfig config = new ApplicationConfig();
	private final ApplicationContext context = new ApplicationContext();
	private final ConverterFactory converterFactory = new ConverterFactory();
	private ClassLoader classLoader;
	private ControllerFactory controllerFactory;
	private String domains[];
	private String name;
	private Loader loader;
	private Locale locale;
	private InitializerFactory initializerFactory;
	private InterceptorFactory interceptorFactory;
	private MessageFactory messageFactory;
	private ResponderFactory responderFactory;
	private SessionFactory sessionFactory;
	
	public Application(File base) {
		this.base = base;
		this.name = base.getName();
	}
	
	private void initClassLoader() throws Exception {
		if(log.isTraceEnabled())
			log.trace("initializing ClassLoader for application {}", this);
		
		//
		// Enhance Application
		//
		Enhancer enhancer = new Enhancer(this);
		enhancer.enhance();
		//
		// Initialize Application loader
		//
		loader = new Loader(this);
		loader.load();
		
		classLoader = loader.getClassLoader();
	}
	
	private void initConfig() throws Exception {
		File file = new File(base, ApplicationConfig.CONFIG_FILENAME);
		try {
			if(file.exists()) {
				config.load(new FileInputStream(file));
			}
		}
		catch(Exception e) {
			throw new RuntimeException("failed to load application configuration file: "+file.getAbsolutePath(), e);
		}
		
		//
		// set directories
		//
		config.setBaseDirectory(base);
		File classesdir = new File(base, "classes");
		if(!classesdir.exists())
			classesdir.mkdirs();
		config.setClassesDirectory(classesdir);
		File libdir = new File(base, "lib");
		if(!libdir.exists())
			libdir.mkdirs();
		config.setLibDirectory(libdir);
		File privatedir = new File(base, "private");
		if(!privatedir.exists())
			privatedir.mkdirs();
		config.setPrivateDirectory(privatedir);
		File publicdir = new File(base, "public");
		if(!publicdir.exists())
			publicdir.mkdirs();
		config.setPublicDirectory(publicdir);
		File tmpdir = new File(base, "tmp");
		if(!tmpdir.exists())
			tmpdir.mkdirs();
		config.setTmpDirectory(tmpdir);
		File workdir = new File(base, "work");
		if(workdir.exists()) {
			// clear this directory
			if(log.isTraceEnabled())
				log.trace("deleting work directory {} ... ", workdir.getAbsolutePath());
			FileUtils.delete(workdir);
		}
		if(!workdir.exists())
			workdir.mkdirs();
		config.setWorkDirectory(workdir);
		
		List<String> domains = new ArrayList<String>();
		// auxiliary domain map string for removing duplicate entries
		HashMap<String,String> map = new HashMap<String,String>();
		// first entry is the application name... which also can be an alias to final domain
		// convention name is always the domain name... www.unagisushi.com
		// the directory under the as/applications shall be something like:
		// applications/unagisushi.com
		//
		domains.add(name);
		map.put(name, name);
		// parsing information from the configuration file
		String domainAliases = config.getProperty(ApplicationConfig.DOMAINS_KEY);
		if(domainAliases != null) {
			String ds[] = domainAliases.split(",");
			for(int i=0 ; i < ds.length; i++) {
				// domain name and aliases shall be always lowered case so facilitates case sensitive searches
				// white spaces also removed from the before and after
				String domain = ds[i].toLowerCase().trim();
				if(map.containsKey(domain))
					continue;
				domains.add(domain);
				map.put(domain, domain);
			}
		}
		this.domains = domains.toArray(new String[0]);
		
		if(log.isTraceEnabled())
			log.trace("application responsible for domains: {}", domains);
		
		// locale configuration
		String localeString = config.getString(ApplicationConfig.LOCALE_KEY, Locale.getDefault().toString());
		locale = StringUtils.toLocale(localeString);
		config.setLocale(locale);
		
		// Session configuration
		Long sessionExpire = config.getLong(ApplicationConfig.SESSION_EXPIRE_KEY, 300) * 1000;
		config.setSessionExpire(sessionExpire);
		String sessionIdKey = config.getString(ApplicationConfig.SESSION_IDKEY_KEY, "SSID");
		config.setSessionIdKey(sessionIdKey);
		Integer sessionPoolSize = config.getInt(ApplicationConfig.SESSION_POOL_SIZE_KEY, 50);
		config.setSessionPoolSize(sessionPoolSize);
		
		// template cache
		Boolean templateCache = config.getBoolean(ApplicationConfig.TEMPLATE_CACHE, true);
		config.setTemplateCache(templateCache);
		// template version
		String templateVersion = config.getString(ApplicationConfig.TEMPLATE_VERSION, "2.3.22");
		config.setTemplateVersion(templateVersion);
	}
	
	private void initContext() throws Exception {
		if(log.isTraceEnabled())
			log.trace("initializing ApplicationContext for application {}", this);
		// setting up context
		context.clear();
		context.put(ApplicationContext.HOME, base.getAbsolutePath());
		context.put(ApplicationContext.LOCALE, config.getLocale());
		context.put(ApplicationContext.PROPERTIES, config);
	}
	
	public void init() throws Exception {
		initConfig();
		initContext();
		initClassLoader();
		
		messageFactory = new ResourceBundleMessageFactory();
		messageFactory.load(config.getBaseDirectory());
		
		responderFactory = new ResponderFactory();
		responderFactory.init(this);
		
		sessionFactory = new SessionFactoryImpl();
		sessionFactory.start(config);
		
		Thread.currentThread().setContextClassLoader(loader.getClassLoader());

		/* Initializers */
		this.initializerFactory = new InitializerFactory(this);
		for(Class<?> clazz: loader.getInitializers()) {
			initializerFactory.register(clazz);
		}		
		initializerFactory.init();

		/* Interceptors */
		interceptorFactory = new InterceptorFactory(this);
		for(Class<?> clazz: loader.getInterceptors()) {
			if(log.isTraceEnabled())
				log.trace("registering @Interceptor "+clazz.getName());
			interceptorFactory.register(clazz);
		}

		/* Controllers */
		controllerFactory = new ControllerFactory(this);
		if(log.isTraceEnabled())
			log.trace("controllerFactory: {}", controllerFactory);
		for(Class<?> clazz: loader.getControllers()) {
			if(log.isTraceEnabled())
				log.trace("registering @Controller "+clazz.getName());
			controllerFactory.register(clazz);
		}

		if(log.isInfoEnabled())
			log.info("application {} started and running", this);
	}
	
	public void start() throws Exception {
		if(log.isInfoEnabled())
			log.info("starting "+this);
		init();
	}
	
	public void stop() throws Exception {
		if(initializerFactory == null) {
			if(log.isDebugEnabled())
				log.debug("application not initialized for stop procedure... InitializerFactory is null");
			return;
		}
		initializerFactory.destroy();
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	
	public ApplicationConfig getConfig() {
		return config;
	}
	
	public ApplicationContext getContext() {
		return context;
	}
	
	public ControllerFactory getControllerFactory() {
		return controllerFactory;
	}
	
	public ConverterFactory getConverterFactory() {
		return converterFactory;
	}
	
	public String[] getDomains() {
		return domains;
	}
	
	public InterceptorFactory getInterceptorFactory() {
		return interceptorFactory;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public MessageFactory getMessageFactory() {
		return messageFactory;
	}
	
	public ResponderFactory getResponderFactory() {
		return responderFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Application-").append(name);
		return sb.toString();
	}
}