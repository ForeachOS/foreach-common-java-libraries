package com.foreach.spring.logging;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class for Log4j. This class contains utility methods such as to retrieve registered loggers.
 *
 * @version 1.0
 */
public class Log4JUtils
{
	protected Log4JUtils() {
		// protected constructor, so that this class can be extended.
	}

	/**
	 * To get a list of all registered loggers in current application.
	 * Returned list of loggers will be sorted by its name
	 *
	 * @return List of loggers
	 */
	public static List<Logger> getClassLoggers() {
		List<String> loggerNames = new ArrayList<String>();
		Enumeration enumeration = LogManager.getCurrentLoggers();
		while ( enumeration.hasMoreElements() ) {
			Logger logger = (Logger) enumeration.nextElement();
			loggerNames.add( logger.getName() );
		}

		Collections.sort( loggerNames );

		List<Logger> result = new ArrayList<Logger>();

		for ( String loggerName : loggerNames ) {
			result.add( Logger.getLogger( loggerName ) );
		}

		return result;
	}
}