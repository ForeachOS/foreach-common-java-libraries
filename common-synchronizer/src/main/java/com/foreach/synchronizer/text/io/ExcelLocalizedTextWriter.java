package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.Language;
import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.spring.localization.text.LocalizedText;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelLocalizedTextWriter implements LocalizedTextWriter {

    private static final String templateResource = "common-synchronizer\\target\\classes\\com\\foreach\\synchronizer\\text\\template.xml";
//    private static final String templateResource = "com\\foreach\\synchronizer\\text\\template.xml";

    public static final String SS_STYLE_ID = "ss:StyleID";
    public static final String NEWLINE = "\n      ";

    private OutputStream outputStream;

    private DecimalFormat formatter;

    public ExcelLocalizedTextWriter( OutputStream outputStream ) {
        this.outputStream = outputStream;
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator( '.' );
        formatter = new DecimalFormat( "0.00", symbols );
    }

    public void write( List<LocalizedText> localizedTexts ) {
        final XMLStreamWriter writer;
	    ByteArrayOutputStream xml = new ByteArrayOutputStream(  );

        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        try {
            writer = factory.createXMLStreamWriter( xml, "UTF-8" );

            writer.writeStartElement( "Table" );
            writer.writeAttribute( "ss:ExpandedColumnCount", "15" );
            writer.writeAttribute( "ss:FullColumns", "1" );
            writer.writeAttribute( "ss:FullRows", "1" );
            writer.writeAttribute( "ss:DefaultRowHeight", "15" );
            writer.writeAttribute( "ss:ExpandedRowCount", (( Integer ) (localizedTexts.size() + 1)).toString() );
            writeColumnInitialization( writer, "s66", "150", 11 );

            WriteTitleRow( writer );

            for( LocalizedText item : localizedTexts ) {
                WriteRow( writer, item );
            }
            writer.writeEndElement();
            writer.flush();
            writer.close();
        } catch ( XMLStreamException xmle ) {
            throw new RuntimeException( xmle );
        }

        try {
            InputStream templateInputStream = this.getClass().getResourceAsStream( "/com/foreach/synchronizer/text/template.xml" );
            String excel = IOUtils.toString( templateInputStream, "UTF-8" );

            Pattern pattern = Pattern.compile( "<Worksheet.*>(.*)<WorksheetOptions", Pattern.MULTILINE | Pattern.DOTALL );
            Matcher matcher = pattern.matcher( excel );

            if( matcher.find() ) {
                String original = matcher.group( 1 );
                excel = excel.replace( original, xml.toString( "UTF-8" ) );
            }

	        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( outputStream , "UTF-8"));
	        bw.write(excel);
            bw.flush();
            bw.close();
        } catch ( IOException ioe ) {
            throw new RuntimeException( ioe );
        }
    }

    private void writeColumnInitialization( XMLStreamWriter writer, String style, String width, Integer columnCount ) throws XMLStreamException {
        String span = (--columnCount).toString();

        writer.writeCharacters( NEWLINE );
        writer.writeStartElement( "Column" );
        writer.writeAttribute( SS_STYLE_ID, style );
        writer.writeAttribute( "ss:AutoFitWidth", "0" );
        writer.writeAttribute( "ss:Width", width );
        writer.writeAttribute( "ss:Span", span );
        writer.writeEndElement();
    }

    private void WriteRow( XMLStreamWriter writer, LocalizedText item ) throws XMLStreamException {
        writer.writeStartElement( "Row" );
        writer.writeAttribute( "ss:AutoFitHeight", "0" );
        writer.writeAttribute( "ss:Height", formatter.format( calculateHeight( item ) ) );
        writeLabel( writer, item.getApplication() );
        writeLabel( writer, item.getGroup() );
        writeLabel( writer, item.getLabel() );
        for( Language language : LanguageConfigurator.getLanguages() ) {
            writeCell( writer, item.getFieldsForLanguage( language ).getText() );
        }
        Boolean isUsed = item.isUsed();
        Boolean isAutoGenerated = item.isAutoGenerated();
        writeLabel( writer, isUsed.toString() );
        writeLabel( writer, isAutoGenerated.toString() );

        DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        String created = item.getCreated() == null ? "" : dateFormat.format( item.getCreated() );
        String updated = item.getUpdated() == null ? "" : dateFormat.format( item.getUpdated() );
        writeDate( writer, created );
        writeDate( writer, updated );

        writer.writeEndElement();
    }

    private void WriteTitleRow( XMLStreamWriter writer ) throws XMLStreamException {
        writer.writeStartElement( "Row" );
        writer.writeAttribute( "ss:AutoFitHeight", "0" );
        writer.writeAttribute( "ss:Height", "30.75" );

        writeHeader( writer, "Application" );
        writeHeader( writer, "Group" );
        writeHeader( writer, "Label" );
        for( Language language : LanguageConfigurator.getLanguages() ) {
            writeHeader( writer, language.getName() );
        }

        writeHeader( writer, "used" );
        writeHeader( writer, "autoGenerated" );
        writeHeader( writer, "created" );
        writeHeader( writer, "updated" );
        writer.writeEndElement();
    }

    public void close() throws IOException {
        IOUtils.closeQuietly( outputStream );
    }

    private void writeHeader( XMLStreamWriter xml, String text ) throws XMLStreamException {
        xml.writeStartElement( "Cell" );
        xml.writeAttribute( SS_STYLE_ID, "s76" );

        xml.writeStartElement( "Data" );
        xml.writeAttribute( "ss:Type", "String" );
        xml.writeCharacters( text );
        xml.writeEndElement();

        xml.writeEndElement();
    }

    private void writeLabel( XMLStreamWriter xml, String text ) throws XMLStreamException {
        xml.writeStartElement( "Cell" );

        xml.writeStartElement( "Data" );
        xml.writeAttribute( "ss:Type", "String" );
        xml.writeCharacters( (text == null) ? "" : text );
        xml.writeEndElement();

        xml.writeEndElement();
    }

    private void writeDate( XMLStreamWriter xml, String text ) throws XMLStreamException {
        xml.writeStartElement( "Cell" );
        xml.writeAttribute( SS_STYLE_ID, "s80" );

        xml.writeStartElement( "Data" );
        xml.writeAttribute( "ss:Type", "String" );
        xml.writeCharacters( (text == null) ? "" : text );
        xml.writeEndElement();

        xml.writeEndElement();
    }

    private void writeCell( XMLStreamWriter xml, String text ) throws XMLStreamException {
        xml.writeStartElement( "Cell" );
        xml.writeAttribute( SS_STYLE_ID, "s67" );

        xml.writeStartElement( "Data" );
        xml.writeAttribute( "ss:Type", "String" );
        xml.writeCharacters( (text == null) ? "" : text );
        xml.writeEndElement();

        xml.writeEndElement();
    }

    private BigDecimal calculateHeight( LocalizedText item ) {
        int textLength = 0;
        for( Language language : LanguageConfigurator.getLanguages() ) {
            textLength = Math.max( textLength, StringUtils.length( item.getFieldsForLanguage( language ).getText() ) );
        }
        double multiplier = 1;
        if( textLength > 50 ) {
            multiplier = Math.ceil( textLength / 50D );
        }
        return new BigDecimal( "20.25" ).multiply( new BigDecimal( multiplier ) );
    }
}
