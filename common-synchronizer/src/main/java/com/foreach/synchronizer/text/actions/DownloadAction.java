package com.foreach.synchronizer.text.actions;

import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextService;
import com.foreach.synchronizer.text.io.LocalizedTextFileHandler;
import com.foreach.synchronizer.text.io.LocalizedTextOutputFormat;
import com.foreach.synchronizer.text.io.LocalizedTextWriter;
import com.foreach.synchronizer.text.io.LocalizedTextWriterFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.List;

@Component
public class DownloadAction implements SynchronizerAction {

    public static final String OPTION_OUTPUT_DIR = "output-dir";
    public static final String SHORT_OPTION_OUTPUT_DIR = "o";
    public static final String OPTION_FORMAT = "format";
    public static final String SHORT_OPTION_FORMAT = "f";
    private static final LocalizedTextOutputFormat DEFAULT_OUTPUT_FORMAT = LocalizedTextOutputFormat.XML;

    @Autowired
    private LocalizedTextWriterFactory localizedTextWriterFactory;

    @Autowired
    private LocalizedTextService localizedTextService;

    @Autowired
    private LocalizedTextFileHandler localizedTextFileHandler;

    public Options getCliOptions() {
        Options options = new Options();
        options.addOption( SHORT_OPTION_OUTPUT_DIR, OPTION_OUTPUT_DIR, true, "the output directory to save the files to" );
        options.addOption( SHORT_OPTION_FORMAT, OPTION_FORMAT, true, "the output format (default=" + DEFAULT_OUTPUT_FORMAT.name() + ")" );
        return options;
    }

    public String getActionName() {
        return "download";
    }

    public void execute( CommandLine commandLine ) {
        String outputDir = commandLine.getOptionValue( OPTION_OUTPUT_DIR );
        LocalizedTextOutputFormat outputFormat = getOutputFormat( commandLine );
        writeToFiles( outputDir, outputFormat );
    }

    private LocalizedTextOutputFormat getOutputFormat( CommandLine commandLine ) {
        String formatAsString = commandLine.getOptionValue( OPTION_FORMAT );
        if( formatAsString == null ) {
            return DEFAULT_OUTPUT_FORMAT;
        } else {
            return LocalizedTextOutputFormat.valueOf( formatAsString.toUpperCase() );
        }
    }

    public void writeToFiles( String outputDirectory, LocalizedTextOutputFormat outputFormat ) {
        for( String application : localizedTextService.getApplications() ) {
            for( String group : localizedTextService.getGroups( application ) ) {
                LocalizedTextWriter writer = null;
                try {
                    OutputStream outputStream = localizedTextFileHandler.getOutputStream( outputDirectory, application, group, outputFormat );
                    List<LocalizedText> localizedTextItems = localizedTextService.getLocalizedTextItems( application, group );
                    writer = localizedTextWriterFactory.createLocalizedTextWriter( outputFormat, outputStream );
                    writer.write( localizedTextItems );
                } finally {
                    IOUtils.closeQuietly( writer );
                }
            }
        }
    }
}
