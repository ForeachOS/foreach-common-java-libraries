package com.foreach.synchronizer.text.io;

import com.foreach.synchronizer.text.TextSynchronizerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LocalizedTextFileHandlerImpl implements LocalizedTextFileHandler {

	private static final Logger LOG = LoggerFactory.getLogger( LocalizedTextFileHandlerImpl.class );

	public OutputStream getOutputStream( String outputDirectory, String application, String group, LocalizedTextOutputFormat outputFormat ) {
		File outputFile = getOutputFile( outputDirectory, application, group, outputFormat );
		try {
			return new FileOutputStream( outputFile );
		} catch ( IOException e ) {
			LOG.error( e.getMessage(), e );
			throw new TextSynchronizerException( "Unable to write to file: " + outputFile.getAbsolutePath() + "!" );
		}
	}

	public File getOutputFile( String outputDirectory, String application, String group, LocalizedTextOutputFormat outputFormat ) {
		File outputFile = new File( outputDirectory, getFileName( application, group, outputFormat ) );
		if ( outputFile.exists() && outputFile.isDirectory() ) {
			throw new TextSynchronizerException( "File " + outputFile.getAbsolutePath() + " is a directory!" );
		}
		return outputFile;
	}

	public String getFileName( String application, String group, LocalizedTextOutputFormat outputFormat ) {
		String fileName = application + "." + group;
		fileName = fileName.toLowerCase() + "." + outputFormat.toString().toLowerCase();
		return fileName;
	}
}