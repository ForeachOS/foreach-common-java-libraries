package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.spring.localization.text.LocalizedText;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestXmlLocalizedTextReader extends BaseLocalizedTextWriterTest {

    private String expectedOutput = "<list>\n" +
            "  <text>\n" +
            "    <fields>\n" +
            "      <language>EN</language>\n" +
            "      <text>This is a test!</text>\n" +
            "    </fields>\n" +
            "    <fields>\n" +
            "      <language>NL</language>\n" +
            "      <text>Dit is een test!</text>\n" +
            "    </fields>\n" +
            "    <application>myApp</application>\n" +
            "    <group>myGroup</group>\n" +
            "    <label>my.label</label>\n" +
            "    <used>false</used>\n" +
            "    <autoGenerated>false</autoGenerated>\n" +
            "    <created>2012-01-31 23:00:00.0 UTC</created>\n" +
            "  </text>\n" +
            "</list>";

    @Before
    public void setup() {
        new LanguageConfigurator( TestLanguage.class );
    }

    @Test
    public void testRead() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream( expectedOutput.getBytes( "UTF-8" ) );
        XmlLocalizedTextReader xmlLocalizedTextReader = new XmlLocalizedTextReader( inputStream );
        List<LocalizedText> read = new ArrayList<LocalizedText>( xmlLocalizedTextReader.read() );
        List<LocalizedText> expected = createLocalizedTexts();
        assertEquals( expected, read );
        for( int i = 0; i < expected.size(); i++ ) {
            LocalizedText readField = read.get( i );
            LocalizedText expectedField = expected.get( i );
            assertEquals( expectedField.getFields(), readField.getFields() );
        }
    }

}