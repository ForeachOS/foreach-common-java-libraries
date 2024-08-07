/*
 * Copyright 2014 the original author or authors
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
package com.foreach.common.spring.logging;

import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * <p>The LogbackConfigurer can be used to configure logback based on one or more
 * xml configuration resources. The configurer must be created as a Spring bean as
 * the actual configuration will be done in the @PostConstruct of the bean.</p>
 * <p>Example usage in Spring {@literal @}Configuration:</p>
 * <pre>
 *   {@literal @}Bean
 *   public LogbackConfigurer logbackConfigurer( {@literal @}Value("${log.dir}") String logDir,
 *                                               {@literal @}Value("${log.config}") Resource baseConfig,
 *                                               {@literal @}Value("${log.config.extend}") Resource envConfig ) {
 *     return new LogbackConfigurer( logDir, baseConfig, envConfig );
 *   }
 * </pre>
 * <p>Properties configuration:</p>
 * <pre>
 *   # logging configuration
 *   log.dir=/logs/myapplication
 *   log.config=classpath:/config/logback.xml
 *   log.config.extend=classpath:/config/development/logback.xml
 * </pre>
 * <p><strong>Note:</strong> number of resources passed in is variable, and resources do not have to exist as
 * they will be ignored if they don't.  In Spring xml configuration, resources should be passed in as a collection.</p>
 */
public class LogbackConfigurer extends LogbackConfigurerAdapter
{
	private final String logDir;
	private final Collection<Resource> configurationResources;

	/**
	 * Create a configurer with the specified log directory and one or more XML resources.
	 * The XML resources should be passed in the order in which they should be applied.
	 * The log directory passed in can be used in the XML resources as ${log.dir} property.
	 *
	 * @param logDir                 Base directory for log files - will be passed as ${log.dir} property.
	 * @param configurationResources One or more XML (Joran style) resources.
	 */
	public LogbackConfigurer( String logDir, Resource... configurationResources ) {
		this.logDir = logDir;
		this.configurationResources = Arrays.asList( configurationResources );
	}

	@Override
	protected void addProperties( Map<String, String> properties ) {
		properties.put( "log.dir", logDir );
	}

	@Override
	protected void addConfigurationResources( Collection<Resource> resources ) {
		resources.addAll( configurationResources );
	}
}
